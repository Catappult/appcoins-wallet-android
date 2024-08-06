package com.asfoundation.wallet.onboarding_new_payment

import android.content.Intent
import android.net.Uri
import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetAnalyticsRevenueValueUseCase
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import java.math.BigDecimal
import javax.inject.Inject

class OnboardingPaymentEvents @Inject constructor(
  private val paymentMethodsAnalytics: PaymentMethodsAnalytics,
  private val billingAnalytics: BillingAnalytics,
  private val revenueValueUseCase: GetAnalyticsRevenueValueUseCase,
  private val analyticsManager: AnalyticsManager
) {

  fun sendPaymentConfirmationEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType
  ) {
    billingAnalytics.sendPaymentConfirmationEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = paymentType.mapToService().transactionType,
      transactionType = transactionBuilder.type,
      action = BillingAnalytics.ACTION_BUY,
      isOnboardingPayment = true
    )
  }

  fun sendPaymentErrorEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType,
    refusalCode: Int? = null,
    refusalReason: String? = null,
    riskRules: String? = null
  ) {
    stopTimingForPurchaseEvent(
      success = false,
      paymentType = paymentType
    )
    billingAnalytics.sendPaymentErrorWithDetailsAndRiskEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = paymentType.mapToService().transactionType,
      transactionType = transactionBuilder.type,
      errorCode = refusalCode.toString(),
      errorDetails = refusalReason,
      riskRules = riskRules,
      isOnboardingPayment = true
    )
  }

  fun sendPaymentErrorMessageEvent(
    errorCode: String? = null,
    errorMessage: String?,
    transactionBuilder: TransactionBuilder,
    paymentMethod: String,
  ) {
    billingAnalytics.sendPaymentErrorWithDetailsAndRiskEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = paymentMethod,
      transactionType = transactionBuilder.type,
      errorCode = errorCode ?: "",
      errorDetails = errorMessage ?: "",
      riskRules = "",
      isOnboardingPayment = true
    )
  }

  fun sendPaymentMethodEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType?,
    action: String
  ) {
    paymentMethodsAnalytics.sendPaymentMethodEvent(
      appPackage = transactionBuilder.domain,
      skuId = transactionBuilder.skuId,
      amount = transactionBuilder.amount().toString(),
      paymentId = paymentType?.mapToService()?.transactionType ?: "other_payment_methods",
      type = transactionBuilder.type,
      action = action,
      isOnboardingPayment = true
    )
  }

  fun sendPurchaseStartWithoutDetailsEvent(transactionBuilder: TransactionBuilder) {
    billingAnalytics.sendPurchaseStartWithoutDetailsEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      transactionType = transactionBuilder.type,
      context = BillingAnalytics.WALLET_PAYMENT_METHOD,
      isOnboardingPayment = true
    )
  }

  fun sendPaymentSuccessFinishEvents(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType
  ) {
    paymentMethodsAnalytics.stopTimingForPurchaseEvent(
      paymentMethod = paymentType.mapToService().transactionType,
      success = true,
      isPreselected = false
    )
    billingAnalytics.sendPaymentEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = paymentType.mapToService().transactionType,
      transactionType = transactionBuilder.type,
      isOnboardingPayment = true
    )
    billingAnalytics.sendRevenueEvent(revenueValueUseCase(transactionBuilder))
  }

  fun sendPaymentSuccessEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType,
    txId: String
  ) {
    stopTimingForPurchaseEvent(
      success = true,
      paymentType = paymentType
    )
    billingAnalytics.sendPaymentSuccessEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = paymentType.mapToService().transactionType,
      transactionType = transactionBuilder.type,
      isOnboardingPayment = true,
      txId = txId,
      valueUsd = transactionBuilder.amountUsd.toString()
    )
  }

  fun sendCarrierBillingConfirmationEvent(transactionBuilder: TransactionBuilder, action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = BillingAnalytics.PAYMENT_METHOD_CARRIER,
      transactionType = transactionBuilder.type,
      action = action,
      isOnboardingPayment = true
    )
  }

  fun sendAdyenPaymentConfirmationEvent(
    transactionBuilder: TransactionBuilder,
    action: String,
    paymentType: String
  ) {
    billingAnalytics.sendPaymentConfirmationEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = paymentType,
      transactionType = transactionBuilder.type,
      action = action,
      isOnboardingPayment = true
    )
  }

  fun sendAdyenPaymentUrlEvent(
    transactionBuilder: TransactionBuilder,
    data: Intent,
    paymentType: String
  ) {
    val amountString = transactionBuilder.amount().toString()
    billingAnalytics.sendPaypalUrlEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = amountString, transactionType = paymentType,
      type = getQueryParameter(data, "type"),
      resultCode = getQueryParameter(data, "resultCode"),
      url = data.dataString,
      isOnboardingPayment = true
    )
  }

  private fun getQueryParameter(data: Intent, parameter: String): String? {
    return Uri.parse(data.dataString).getQueryParameter(parameter)
  }

  fun startTimingForPurchaseEvent() {
    paymentMethodsAnalytics.startTimingForPurchaseEvent()
  }

  private fun stopTimingForPurchaseEvent(success: Boolean, paymentType: PaymentType) {
    paymentMethodsAnalytics.stopTimingForPurchaseEvent(
      paymentMethod = paymentType.mapToService().transactionType,
      success = success,
      isPreselected = false
    )
  }

  fun send3dsStart(type: String?) {
    paymentMethodsAnalytics.send3dsStart(type)
  }

  fun send3dsCancel() {
    paymentMethodsAnalytics.send3dsCancel()
  }

  fun send3dsError(error: String?) {
    paymentMethodsAnalytics.send3dsError(error)
  }

  fun sendPaymentConclusionNavigationEvent(action: String) {
    analyticsManager.logEvent(
      hashMapOf<String, Any>(
        ONBOARDING_PAYMENT to true,
        BillingAnalytics.EVENT_ACTION to action
      ),
      EVENT_WALLET_PAYMENT_CONCLUSION_NAVIGATION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendLocalNavigationToUrlEvents(
    packageName: String, skuId: String?, amount: String, type: String,
    paymentId: String
  ) {
    billingAnalytics.sendPaymentMethodDetailsEvent(
      packageName = packageName,
      skuDetails = skuId,
      value = amount,
      purchaseDetails = paymentId,
      transactionType = type
    )
    billingAnalytics.sendPaymentConfirmationEvent(
      packageName = packageName,
      skuDetails = skuId,
      value = amount,
      purchaseDetails = paymentId,
      transactionType = type,
      action = BillingAnalytics.ACTION_BUY
    )
  }

  fun sendPaymentConfirmationGooglePayEvent(
    transactionBuilder: TransactionBuilder,
  ) {
    billingAnalytics.sendPaymentConfirmationEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
      transactionType = transactionBuilder.type,
      action = BillingAnalytics.ACTION_BUY,
      isOnboardingPayment = true
    )
  }

  fun sendGooglePaySuccessFinishEvents(
    transactionBuilder: TransactionBuilder,
    txId: String
  ) {
    paymentMethodsAnalytics.stopTimingForPurchaseEvent(
      paymentMethod = BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
      success = true,
      isPreselected = false
    )
    billingAnalytics.sendPaymentSuccessEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
      transactionType = transactionBuilder.type,
      txId = txId,
      valueUsd = transactionBuilder.amountUsd.toString()
    )
    billingAnalytics.sendPaymentEvent(
      packageName = transactionBuilder.domain,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = BillingAnalytics.PAYMENT_METHOD_GOOGLE_PAY_WEB,
      transactionType = transactionBuilder.type, isOnboardingPayment = true
    )
    billingAnalytics.sendRevenueEvent(revenueValueUseCase(transactionBuilder))
  }

  fun sendPaymentConclusionEvents(
    packageName: String,
    skuId: String?,
    amount: BigDecimal,
    type: String,
    paymentId: String,
    txId: String,
    amountUsd: BigDecimal
  ) {
    billingAnalytics.sendPaymentEvent(
      packageName = packageName,
      skuDetails = skuId,
      value = amount.toString(),
      purchaseDetails = paymentId,
      transactionType = type
    )
    billingAnalytics.sendPaymentSuccessEvent(
      packageName = packageName,
      skuDetails = skuId,
      value = amount.toString(),
      purchaseDetails = paymentId,
      transactionType = type,
      txId = txId,
      valueUsd = amountUsd.toString()
    )
  }

  fun sendRevenueEvent(fiatAmount: String) = billingAnalytics.sendRevenueEvent(fiatAmount)

  companion object {
    const val EVENT_WALLET_PAYMENT_CONCLUSION_NAVIGATION = "wallet_payment_conclusion_navigation"
    const val BACK_TO_THE_GAME = "back_to_the_game"
    const val EXPLORE_WALLET = "explore_wallet"
    const val ONBOARDING_PAYMENT = "onboarding_payment"
    const val WALLET = "wallet"
  }
}
