package com.asfoundation.wallet.ui.iab.localpayments

import com.asfoundation.wallet.analytics.FacebookEventLogger.EVENT_REVENUE_CURRENCY
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

class LocalPaymentAnalytics(private val analytics: BillingAnalytics,
                            private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                            private val scheduler: Scheduler) {

  fun sendNavigationToUrlEvents(packageName: String, skuId: String?, amount: String, type: String,
                                paymentId: String) {
    analytics.sendPaymentMethodDetailsEvent(packageName, skuId, amount, paymentId, type)
    analytics.sendPaymentConfirmationEvent(packageName, skuId, amount, type, paymentId, "buy")
  }

  fun sendPaymentConclusionEvents(packageName: String, skuId: String?, amount: BigDecimal,
                                  type: String, paymentId: String,
                                  disposable: CompositeDisposable) {
    analytics.sendPaymentEvent(packageName, skuId, amount.toString(), paymentId, type)
    analytics.sendPaymentSuccessEvent(packageName, skuId, amount.toString(), paymentId, type)
    disposable.add(inAppPurchaseInteractor.convertToFiat(amount.toDouble(), EVENT_REVENUE_CURRENCY)
        .subscribeOn(scheduler)
        .doOnSuccess { fiatValue -> analytics.sendRevenueEvent(fiatValue.amount.toString()) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun sendPendingPaymentEvents(packageName: String, skuId: String?, amount: String, type: String,
                               paymentId: String) {
    analytics.sendPaymentEvent(packageName, skuId, amount, paymentId, type)
    analytics.sendPaymentPendingEvent(packageName, skuId, amount, paymentId, type)
  }

}
