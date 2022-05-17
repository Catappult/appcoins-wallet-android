package com.asfoundation.wallet.ui.iab

import cm.aptoide.analytics.AnalyticsManager
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.analytics.TaskTimer
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import javax.inject.Inject

class PaymentMethodsAnalytics @Inject constructor(
  private val analyticsManager: AnalyticsManager,
  private val billingAnalytics: BillingAnalytics,
  private val analyticsSetup: AnalyticsSetup,
  private val taskTimer: TaskTimer
) {

  companion object {
    private const val WALLET = "WALLET"

    const val WALLET_PAYMENT_LOADING_TOTAL = "wallet_payment_loading_total"
    const val WALLET_PAYMENT_LOADING_STEP = "wallet_payment_loading_step"

    const val LOADING_STEP_WALLET_INFO = "get_wallet_info"
    const val LOADING_STEP_CONVERT_TO_FIAT = "convert_to_local_fiat"
    const val LOADING_STEP_GET_PAYMENT_METHODS = "get_payment_methods"
    const val LOADING_STEP_GET_EARNING_BONUS = "get_earning_bonus"
    const val LOADING_STEP_GET_PROCESSING_DATA = "processing_data"

    private const val STEP_ID = "step_id"
    private const val DURATION = "duration"
  }

  fun setGamificationLevel(cachedGamificationLevel: Int) {
    analyticsSetup.setGamificationLevel(cachedGamificationLevel)
  }

  fun sendPurchaseDetailsEvent(appPackage: String, skuId: String?, amount: String, type: String?) {
    billingAnalytics.sendPurchaseDetailsEvent(appPackage, skuId, amount, type)
  }

  fun sendPaymentMethodEvent(
    appPackage: String,
    skuId: String?,
    amount: String,
    paymentId: String,
    type: String?,
    action: String,
    isPreselected: Boolean = false
  ) {
    if (isPreselected) {
      billingAnalytics.sendPreSelectedPaymentMethodEvent(
        appPackage,
        skuId,
        amount,
        paymentId,
        type,
        action
      )
    } else {
      billingAnalytics.sendPaymentMethodEvent(appPackage, skuId, amount, paymentId, type, action)
    }
  }

  fun startTimingForTotalEvent() = taskTimer.start(WALLET_PAYMENT_LOADING_TOTAL)

  fun startTimingForStepEvent(stepId: String) = taskTimer.start(stepId)

  fun stopTimingForTotalEvent() {
    val duration = taskTimer.end(WALLET_PAYMENT_LOADING_TOTAL) ?: return
    analyticsManager.logEvent(
      hashMapOf<String, Any>(DURATION to duration),
      WALLET_PAYMENT_LOADING_TOTAL,
      AnalyticsManager.Action.IMPRESSION,
      WALLET
    )
  }

  fun stopTimingForStepEvent(stepId: String) {
    val duration = taskTimer.end(stepId) ?: return
    analyticsManager.logEvent(
      hashMapOf<String, Any>(DURATION to duration, STEP_ID to stepId),
      WALLET_PAYMENT_LOADING_STEP,
      AnalyticsManager.Action.IMPRESSION,
      WALLET
    )
  }
}
