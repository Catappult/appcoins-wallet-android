package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.appcoins.wallet.core.utils.common.applicationinfo.ApplicationInfoProvider
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class CarrierFeePresenter(
  private val disposables: CompositeDisposable,
  private val view: CarrierFeeView,
  private val data: CarrierFeeData,
  private val navigator: CarrierFeeNavigator,
  private val billingAnalytics: BillingAnalytics,
  private val appInfoProvider: ApplicationInfoProvider,
  private val viewScheduler: Scheduler
) {

  fun present() {
    initializeView()
    handleBackEvents()
    handleNextButton()
  }

  private fun initializeView() {
    view.initializeView(
      data.currency, data.fiatAmount, data.appcAmount, data.skuDescription,
      data.bonusAmount, data.carrierName, data.carrierImage, data.feeFiatAmount
    )
    disposables.add(
      appInfoProvider.getApplicationInfo(data.domain)
        .observeOn(viewScheduler)
        .doOnSuccess { ai ->
          view.setAppDetails(ai.appName, ai.icon)
        }
        .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNextButton() {
    disposables.add(
      view.nextClickEvent()
        .doOnNext {
          sendPaymentConfirmationEvent("next")
          navigator.navigateToPayment(
            data.domain, data.transactionData, data.transactionType,
            data.skuId, data.paymentUrl, data.appcAmount, data.currency, data.bonusAmount,
            data.phoneNumber
          )
        }
        .retry()
        .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleBackEvents() {
    disposables.add(
      view.cancelButtonEvent()
        .mergeWith(view.systemBackEvent())
        .doOnNext {
          sendPaymentConfirmationEvent("back")
          navigator.navigateToPaymentMethods()
        }
        .retry()
        .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun sendPaymentConfirmationEvent(event: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      data.domain, data.skuId,
      data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER, data.transactionType,
      event
    )
  }

  fun stop() = disposables.clear()
}