package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.StringProvider
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import com.asfoundation.wallet.util.safeLet
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.*

class CarrierVerifyPresenter(
    private val disposables: CompositeDisposable,
    private val view: CarrierVerifyView,
    private val data: CarrierVerifyData,
    private val navigator: CarrierVerifyNavigator,
    private val interactor: CarrierInteractor,
    private val appInfoLoader: ApplicationInfoLoader,
    private val stringProvider: StringProvider,
    private val formatter: CurrencyFormatUtils,
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
                  when (error.code) {
                    4001 -> message =
                        stringProvider.getString(R.string.purchase_carrier_error_minimum,
                            formatFiatValue(error.value, data.currency))
                    4002 -> message =
                        stringProvider.getString(R.string.purchase_carrier_error_maximum,
                            formatFiatValue(error.value, data.currency))
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

  private fun formatFiatValue(value: BigDecimal, currencyCode: String): String {
    val currencySymbol = Currency.getInstance(currencyCode).symbol
    var scaledBonus = value.stripTrailingZeros()
        .setScale(CurrencyFormatUtils.FIAT_SCALE, BigDecimal.ROUND_DOWN)
    var newCurrencyString = currencySymbol
    if (scaledBonus < BigDecimal("0.01")) {
      newCurrencyString = "~$currencySymbol"
    }
    scaledBonus = scaledBonus.max(BigDecimal("0.01"))
    val formattedBonus = formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    return newCurrencyString + formattedBonus
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