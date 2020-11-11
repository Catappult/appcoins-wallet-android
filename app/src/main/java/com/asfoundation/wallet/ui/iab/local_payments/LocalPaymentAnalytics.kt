package com.asfoundation.wallet.ui.iab.local_payments

import com.asfoundation.wallet.analytics.FacebookEventLogger.EVENT_REVENUE_CURRENCY
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

class LocalPaymentAnalytics(private val analytics: BillingAnalytics,
                            private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                            private val scheduler: Scheduler) {


  fun sendPaymentMethodDetailsEvent(domain: String, skuId: String?, amount: String,
                                    type: String, paymentId: String) {
    analytics.sendPaymentMethodDetailsEvent(domain, skuId, amount, paymentId, type)
  }


  fun sendPaymentEvent(domain: String, skuId: String?, amount: String,
                       type: String, paymentId: String) {
    analytics.sendPaymentEvent(domain, skuId, amount, paymentId, type)
  }

  fun sendRevenueEvent(disposable: CompositeDisposable, amount: BigDecimal) {
    disposable.add(inAppPurchaseInteractor.convertToFiat(amount.toDouble(),
        EVENT_REVENUE_CURRENCY)
        .subscribeOn(scheduler)
        .doOnSuccess { fiatValue -> analytics.sendRevenueEvent(fiatValue.amount.toString()) }
        .subscribe())
  }

  fun sendPaymentConfirmationEvent(domain: String, skuId: String?, amount: String, type: String,
                                   paymentId: String) {
    analytics.sendPaymentConfirmationEvent(domain, skuId, amount, type, paymentId, "buy")
  }

  fun sendPaymentConclusionEvent(domain: String, skuId: String?, amount: String, type: String,
                                 paymentId: String) {
    analytics.sendPaymentSuccessEvent(domain, skuId, amount, paymentId, type)
  }

  fun sendPaymentPendingEvent(domain: String, skuId: String?, amount: String, type: String,
                              paymentId: String) {
    analytics.sendPaymentPendingEvent(domain, skuId, amount, paymentId, type)
  }

}
