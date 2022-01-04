package com.asfoundation.wallet.verification.ui.credit_card

import cm.aptoide.analytics.AnalyticsManager
import java.util.*
import javax.inject.Inject

class VerificationAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  companion object {
    private const val WALLET = "WALLET"

    const val START_EVENT = "wallet_verify_start"
    const val INSERT_CARD_EVENT = "wallet_verify_insert_card"
    const val REQUEST_CONCLUSION_EVENT = "wallet_verify_request_conclusion"
    const val CONFIRM_EVENT = "wallet_verify_confirm"
    const val CONCLUSION_EVENT = "wallet_verify_conclusion"
  }

  fun sendStartEvent(action: String) {
    val data = HashMap<String, Any>()
    data["action"] = action
    analyticsManager.logEvent(data, START_EVENT, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendInsertCardEvent(action: String) {
    val data = HashMap<String, Any>()
    data["action"] = action
    analyticsManager.logEvent(data, INSERT_CARD_EVENT, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendRequestConclusionEvent(success: Boolean, errorCode: String?, errorReason: String?) {
    val data = HashMap<String, Any>()
    data["status"] = if (success) "success" else "fail"
    errorCode?.let { data["error_code"] = errorCode }
    errorReason?.let { data["error_reason"] = errorReason }
    analyticsManager.logEvent(data, REQUEST_CONCLUSION_EVENT, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendConfirmEvent(action: String) {
    val data = HashMap<String, Any>()
    data["action"] = action
    analyticsManager.logEvent(data, CONFIRM_EVENT, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendConclusionEvent(success: Boolean, errorCode: String?, errorReason: String?) {
    val data = HashMap<String, Any>()
    data["status"] = if (success) "success" else "fail"
    errorCode?.let { data["error_code"] = errorCode }
    errorReason?.let { data["error_reason"] = errorReason }
    analyticsManager.logEvent(data, CONCLUSION_EVENT, AnalyticsManager.Action.CLICK, WALLET)
  }
}