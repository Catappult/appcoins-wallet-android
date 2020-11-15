package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoModel
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

class CarrierConfirmPresenter(private val disposables: CompositeDisposable,
                              private val view: CarrierConfirmView,
                              private val data: CarrierConfirmData,
                              private val navigator: CarrierConfirmNavigator,
                              private val interactor: CarrierInteractor,
                              private val billingAnalytics: BillingAnalytics,
                              private val appInfoLoader: ApplicationInfoLoader,
                              private val logger: Logger,
                              private val viewScheduler: Scheduler,
                              private val ioScheduler: Scheduler) {

  private lateinit var transactionBuilder: TransactionBuilder

  companion object {
    private val TAG = CarrierConfirmPresenter::class.java.simpleName
  }

  fun present() {
    initializeView()
    handleBackEvents()
    handleNextButton()
  }

  private fun initializeTransaction(): Single<TransactionBuilder> {
    return interactor.getTransactionBuilder(data.transactionData)
        .doOnSuccess { transactionBuilder -> this.transactionBuilder = transactionBuilder }
        .subscribeOn(ioScheduler)
  }

  private fun initializeView() {
    disposables.add(
        Single.zip(appInfoLoader.getApplicationInfo(data.domain), initializeTransaction(),
            BiFunction { ai: ApplicationInfoModel, _: TransactionBuilder -> ai })
            .observeOn(viewScheduler)
            .doOnSuccess { ai ->
              view.initializeView(ai.appName, ai.icon, data.currency, data.totalFiatAmount,
                  data.totalAppcAmount, data.skuDescription, data.bonusAmount, data.carrierName,
                  data.carrierImage, data.feeFiatAmount)
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNextButton() {
    disposables.add(
        view.nextClickEvent()
            .doOnNext {
              sendPaymentConfirmationEvent("buy")
              navigator.navigateToPayment(data.domain, data.transactionData, data.transactionType,
                  data.paymentUrl)
            }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleBackEvents() {
    disposables.add(
        view.backEvent()
            .doOnNext {
              interactor.cancelTransaction(data.uid, data.domain)
              sendPaymentConfirmationEvent("back")
              navigator.navigateBack()
            }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun sendPaymentConfirmationEvent(event: String) {
    billingAnalytics.sendPaymentConfirmationEvent(data.domain, transactionBuilder.skuId,
        transactionBuilder.amount()
            .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER, transactionBuilder.type, event)
  }


  fun stop() = disposables.clear()
}