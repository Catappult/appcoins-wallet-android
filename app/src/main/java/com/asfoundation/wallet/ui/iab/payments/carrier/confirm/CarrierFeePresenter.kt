package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class CarrierFeePresenter(private val disposables: CompositeDisposable,
                          private val view: CarrierFeeView,
                          private val data: CarrierFeeData,
                          private val navigator: CarrierFeeNavigator,
                          private val billingAnalytics: BillingAnalytics,
                          private val appInfoLoader: ApplicationInfoLoader,
                          private val viewScheduler: Scheduler,
                          private val ioScheduler: Scheduler) {

  fun present() {
    initializeView()
    handleBackEvents()
    handleNextButton()
  }

  private fun initializeView() {
    disposables.add(
        appInfoLoader.getApplicationInfo(data.domain)
            .observeOn(viewScheduler)
            .doOnSuccess { ai ->
              view.initializeView(ai.appName, ai.icon, data.currency, data.fiatAmount,
                  data.appcAmount, data.skuDescription, data.bonusAmount, data.carrierName,
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
                  data.skuId, data.paymentUrl, data.appcAmount, data.currency, data.bonusAmount)
            }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleBackEvents() {
    disposables.add(
        view.backEvent()
            .doOnNext {
              sendPaymentConfirmationEvent("back")
              navigator.navigateToPaymentMethods()
            }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun sendPaymentConfirmationEvent(event: String) {
    billingAnalytics.sendPaymentConfirmationEvent(data.domain, data.skuId,
        data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER, data.transactionType,
        event)
  }


  fun stop() = disposables.clear()
}