package com.asfoundation.wallet.onboarding_new_payment

import android.content.Intent
import android.net.Uri
import cm.aptoide.analytics.AnalyticsManager
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetAnalyticsRevenueValueUseCase
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
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
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type,
      "buy",
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
    stopTimingForPurchaseEvent(success = false, paymentType)
    billingAnalytics.sendPaymentErrorWithDetailsAndRiskEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type,
      refusalCode.toString(),
      refusalReason,
      riskRules,
      isOnboardingPayment = true
    )
  }

  fun sendPaymentMethodEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType,
    action: String
  ) {
    paymentMethodsAnalytics.sendPaymentMethodEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type,
      action,
      isOnboardingPayment = true
    )
  }

  fun sendPurchaseStartWithoutDetailsEvent(transactionBuilder: TransactionBuilder) {
    billingAnalytics.sendPurchaseStartWithoutDetailsEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      transactionBuilder.type,
      BillingAnalytics.RAKAM_PAYMENT_METHOD,
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
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type,
      isOnboardingPayment = true
    )
    billingAnalytics.sendRevenueEvent(revenueValueUseCase(transactionBuilder))
  }

  fun sendPaymentSuccessEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType
  ) {
    stopTimingForPurchaseEvent(success = true, paymentType)
    billingAnalytics.sendPaymentSuccessEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type,
      isOnboardingPayment = true
    )
  }

  fun sendCarrierBillingConfirmationEvent(transactionBuilder: TransactionBuilder, action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount()
        .toString(),
      BillingAnalytics.PAYMENT_METHOD_CARRIER,
      transactionBuilder.type,
      action,
      isOnboardingPayment = true
    )
  }

  fun sendPayPalConfirmationEvent(transactionBuilder: TransactionBuilder, action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount()
        .toString(),
      "paypal",
      transactionBuilder.type,
      action,
      isOnboardingPayment = true
    )
  }

  fun sendPaypalUrlEvent(transactionBuilder: TransactionBuilder, data: Intent) {
    val amountString = transactionBuilder.amount()
      .toString()
    billingAnalytics.sendPaypalUrlEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      amountString, "PAYPAL",
      getQueryParameter(data, "type"),
      getQueryParameter(data, "resultCode"),
      data.dataString,
      isOnboardingPayment = true
    )
  }

  fun getQueryParameter(data: Intent, parameter: String): String? {
    return Uri.parse(data.dataString)
      .getQueryParameter(parameter)
  }

  fun startTimingForPurchaseEvent() {
    paymentMethodsAnalytics.startTimingForPurchaseEvent()
  }

  private fun stopTimingForPurchaseEvent(success: Boolean, paymentType: PaymentType) {
    paymentMethodsAnalytics.stopTimingForPurchaseEvent(
      paymentType.mapToService().transactionType,
      success,
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

  fun sendBackToTheGameEvent(conclusionPoint: String) {
    analyticsManager.logEvent(
      hashMapOf<String, Any>(
        ONBOARDING_PAYMENT to true,
        EVENT_WALLET_PAYMENT_CONCLUSION_POINT to conclusionPoint,
      ),
      EVENT_WALLET_METHOD_ONBOARDDING,
      AnalyticsManager.Action.CLICK,
      BACK_TO_THE_GAME
    )
  }

  fun sendExploreWalletEvent(conclusionPoint: String) {
    analyticsManager.logEvent(
      hashMapOf<String, Any>(
        ONBOARDING_PAYMENT to true,
        EVENT_WALLET_PAYMENT_CONCLUSION_POINT to conclusionPoint,
      ),
      EVENT_WALLET_PAYMENT_EXPLORE_WALLET,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  companion object {
    const val EVENT_WALLET_METHOD_ONBOARDDING = "wallet_payment_method"
    const val EVENT_WALLET_PAYMENT_EXPLORE_WALLET = "wallet_payment_explore_wallet"
    const val ONBOARDING_PAYMENT = "onboarding_payment"
    const val EVENT_WALLET_PAYMENT_CONCLUSION_POINT = "wallet_payment_conclusion_point"
    const val WALLET = "wallet"
    const val BACK_TO_THE_GAME = "back_to_the_game"
  }
}