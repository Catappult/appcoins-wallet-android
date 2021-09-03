package com.asfoundation.wallet.ui.iab.localpayments

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import java.math.BigDecimal

class LocalPaymentAnalytics(private val analytics: BillingAnalytics) {

  fun sendNavigationToUrlEvents(packageName: String, skuId: String?, amount: String, type: String,
                                paymentId: String) {
    analytics.sendPaymentMethodDetailsEvent(packageName, skuId, amount, paymentId, type)
    analytics.sendPaymentConfirmationEvent(packageName, skuId, amount, type, paymentId, "buy")
  }

  fun sendPaymentConclusionEvents(packageName: String, skuId: String?, amount: BigDecimal,
                                  type: String, paymentId: String) {
    analytics.sendPaymentEvent(packageName, skuId, amount.toString(), paymentId, type)
    analytics.sendPaymentSuccessEvent(packageName, skuId, amount.toString(), paymentId, type)
  }

  fun sendPendingPaymentEvents(packageName: String, skuId: String?, amount: String, type: String,
                               paymentId: String) {
    analytics.sendPaymentEvent(packageName, skuId, amount, paymentId, type)
    analytics.sendPaymentPendingEvent(packageName, skuId, amount, paymentId, type)
  }

  fun sendRevenueEvent(fiatAmount: String) = analytics.sendRevenueEvent(fiatAmount)
}
