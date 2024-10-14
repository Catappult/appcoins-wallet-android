package com.asfoundation.wallet.ui.iab.localpayments

import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import java.math.BigDecimal
import javax.inject.Inject

class LocalPaymentAnalytics @Inject constructor(private val analytics: BillingAnalytics) {

  fun sendNavigationToUrlEvents(
    packageName: String, skuId: String?, amount: String, type: String,
    paymentId: String
  ) {
    analytics.sendPaymentMethodDetailsEvent(packageName, skuId, amount, paymentId, type)
    analytics.sendPaymentConfirmationEvent(
      packageName,
      skuId,
      amount,
      paymentId,
      type,
      BillingAnalytics.ACTION_BUY
    )
  }

  fun sendPaymentConclusionEvents(
    packageName: String, skuId: String?, amount: BigDecimal,
    type: String, paymentId: String, txId: String,
    amountUsd: BigDecimal
  ) {
    analytics.sendPaymentEvent(packageName, skuId, amount.toString(), paymentId, type)
    analytics.sendPaymentSuccessEvent(
      packageName = packageName,
      skuDetails = skuId,
      value = amount.toString(),
      purchaseDetails = paymentId,
      transactionType = type,
      txId = txId,
      valueUsd = amountUsd.toString()
    )
  }

  fun sendPendingPaymentEvents(
    packageName: String, skuId: String?, amount: String, type: String,
    paymentId: String
  ) {
    analytics.sendPaymentEvent(packageName, skuId, amount, paymentId, type)
    analytics.sendPaymentPendingEvent(packageName, skuId, amount, paymentId, type)
  }

  fun sendRevenueEvent(fiatAmount: String) = analytics.sendRevenueEvent(fiatAmount)
}
