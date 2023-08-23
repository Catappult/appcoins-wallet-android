package com.appcoins.wallet.core.analytics.analytics.legacy

interface EventSender {
  fun sendPurchaseDetailsEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    transactionType: String?,
    isOnboardingPayment: Boolean = false
  )

  fun sendPaymentMethodDetailsEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendActionPaymentMethodDetailsActionEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    action: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendPaymentEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendRevenueEvent(value: String, isOnboardingPayment: Boolean = false)

  fun sendPreSelectedPaymentMethodEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String?,
    action: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendPaymentMethodEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String?,
    action: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendPaymentConfirmationEvent(
    packageName: String?,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String?,
    action: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendPaymentErrorEvent(
    packageName: String,
    skuDetails: String,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    errorCode: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendPaymentErrorWithDetailsEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    errorCode: String,
    errorDetails: String?,
    isOnboardingPayment: Boolean = false
  )

  fun sendPaymentErrorWithDetailsAndRiskEvent(
    packageName: String,
    skuDetails: String,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    errorCode: String,
    errorDetails: String?,
    riskRules: String?,
    isOnboardingPayment: Boolean = false
  )

  fun sendPaymentSuccessEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    isOnboardingPayment: Boolean = false,
    txId: String,
    valueUsd: String
  )

  fun sendPaymentPendingEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendPurchaseStartEvent(
    packageName: String?,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String?,
    context: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendPurchaseStartWithoutDetailsEvent(
    packageName: String?,
    skuDetails: String?,
    value: String,
    transactionType: String?,
    context: String,
    isOnboardingPayment: Boolean = false
  )

  fun sendPaypalUrlEvent(
    packageName: String?,
    skuDetails: String?,
    value: String,
    transactionType: String,
    type: String?,
    resultCode: String?,
    url: String?,
    isOnboardingPayment: Boolean = false
  )
}