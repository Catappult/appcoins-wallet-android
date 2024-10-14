package com.asfoundation.wallet.verification.ui.credit_card

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class VerificationAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  private companion object {
    private const val WALLET = "WALLET"

    //Credit Card
    const val START_EVENT = "wallet_verify_start"
    const val INSERT_CARD_EVENT = "wallet_verify_insert_card"
    const val REQUEST_CONCLUSION_EVENT = "wallet_verify_request_conclusion"
    const val CONFIRM_EVENT = "wallet_verify_confirm"
    const val CONCLUSION_EVENT = "wallet_verify_conclusion"

    //PayPal
    const val PAYPAL_START_EVENT = "wallet_app_verify_paypal_disclaimer_impression"
    const val PAYPAL_START_CLICK_EVENT = "wallet_app_verify_paypal_disclaimer_click"
    const val PAYPAL_CODE_EVENT = "wallet_app_verify_paypal_insert_code_impression"
    const val PAYPAL_CODE_CLICK_EVENT = "wallet_app_verify_paypal_insert_code_click"
    const val PAYPAL_SUCCESS_EVENT = "wallet_app_verify_paypal_success_page_impression"
    const val PAYPAL_SUCCESS_CLICK_EVENT = "wallet_app_verify_paypal_success_page_click"
    const val PAYPAL_ERROR_EVENT = "wallet_app_verify_paypal_error_page_impression"
    const val PAYPAL_ERROR_CLICK_EVENT = "wallet_app_verify_paypal_error_page_click"
  }

  //Credit Card
  fun sendStartEvent(action: String) = sendActionEvent(action, START_EVENT)

  fun sendInsertCardEvent(action: String) = sendActionEvent(action, INSERT_CARD_EVENT)

  fun sendConfirmEvent(action: String) = sendActionEvent(action, CONFIRM_EVENT)

  fun sendConclusionEvent(success: Boolean, errorCode: String?, errorReason: String?) =
    sendStatusEvent(success, errorCode, errorReason, CONCLUSION_EVENT)

  fun sendRequestConclusionEvent(success: Boolean, errorCode: String?, errorReason: String?) =
    sendStatusEvent(success, errorCode, errorReason, REQUEST_CONCLUSION_EVENT)

  //PayPal
  fun sendInitialScreenEvent() = sendScreenEvent(PAYPAL_START_EVENT)

  fun sendInitialScreenEvent(action: String) = sendActionEvent(action, PAYPAL_START_CLICK_EVENT)

  fun sendInsertCodeScreenEvent() = sendScreenEvent(PAYPAL_CODE_EVENT)

  fun sendInsertCodeScreenEvent(action: String) = sendActionEvent(action, PAYPAL_CODE_CLICK_EVENT)

  fun sendSuccessScreenEvent() = sendScreenEvent(PAYPAL_SUCCESS_EVENT)

  fun sendSuccessScreenEvent(action: String) = sendActionEvent(action, PAYPAL_SUCCESS_CLICK_EVENT)

  fun sendErrorScreenEvent() = sendScreenEvent(PAYPAL_ERROR_EVENT)

  fun sendErrorScreenEvent(action: String) = sendActionEvent(action, PAYPAL_ERROR_CLICK_EVENT)


  private fun sendStatusEvent(
    success: Boolean,
    errorCode: String?,
    errorReason: String?,
    event: String
  ) {
    val data = HashMap<String, Any>()
    data["status"] = if (success) "success" else "fail"
    errorCode?.let { data["error_code"] = errorCode }
    errorReason?.let { data["error_reason"] = errorReason }
    analyticsManager.logEvent(data, event, AnalyticsManager.Action.CLICK, WALLET)
  }

  private fun sendActionEvent(action: String, event: String) {
    val data = HashMap<String, Any>()
    data["action"] = action
    analyticsManager.logEvent(data, event, AnalyticsManager.Action.CLICK, WALLET)
  }

  private fun sendScreenEvent(event: String) {
    analyticsManager.logEvent(mapOf(), event, AnalyticsManager.Action.IMPRESSION, WALLET)
  }
}