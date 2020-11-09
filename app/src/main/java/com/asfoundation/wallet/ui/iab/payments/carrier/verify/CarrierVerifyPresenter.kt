package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.util.StringProvider
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import com.asfoundation.wallet.util.safeLet
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class CarrierVerifyPresenter(
    private val disposables: CompositeDisposable,
    private val view: CarrierVerifyView,
    private val data: CarrierVerifyData,
    private val navigator: CarrierVerifyNavigator,
    private val interactor: CarrierInteractor,
    private val appInfoLoader: ApplicationInfoLoader,
    private val stringProvider: StringProvider,
    private val viewScheduler: Scheduler,
    private val ioScheduler: Scheduler) {

  fun present() {
    initializeView()
    handleBackButton()
    handleNextButton()
  }

  private fun initializeView() {
    disposables.add(
        appInfoLoader.getApplicationInfo(data.domain)
            .observeOn(viewScheduler)
            .doOnSuccess { ai ->
              view.initializeView(ai.appName, ai.icon, data.currency, data.fiatAmount,
                  data.appcAmount, data.skuDescription, data.bonusAmount)
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNextButton() {
    disposables.add(
        view.nextClickEvent()
            .doOnNext {
              view.setLoading()
            }
            .flatMapSingle { phoneNumber ->
              interactor.createPayment(phoneNumber, data.domain, data.origin, data.transactionData,
                  data.transactionType, data.currency, data.fiatAmount.toString())
            }
            .observeOn(viewScheduler)
            .doOnNext { paymentModel ->
              if (paymentModel.hasError()) {
                var message = stringProvider.getString(R.string.purchase_carrier_error)
                paymentModel.error?.let { error ->
                  when (error.errorCode) {
                    4001 -> message =
                        stringProvider.getString(R.string.purchase_carrier_error_minimum,
                            error.value)
                    4002 -> message =
                        stringProvider.getString(R.string.purchase_carrier_error_maximum,
                            error.value)
                  }
                }
                navigator.navigateToError(message)
              } else {
                if (paymentModel.status == TransactionStatus.PENDING_USER_PAYMENT) {
                  safeLet(paymentModel.carrier, paymentModel.fee) { carrier, fee ->
                    fee.cost?.let { cost ->
                      navigator.navigateToConfirm(data.domain, paymentModel.paymentUrl,
                          data.currency, data.fiatAmount, data.appcAmount,
                          data.bonusAmount, data.skuDescription, cost.value, carrier.name,
                          carrier.icon)
                    }
                  }
                }
              }
            }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }


  private fun handleBackButton() {
    disposables.add(
        view.backEvent()
            .doOnNext { navigator.navigateBack() }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()

}