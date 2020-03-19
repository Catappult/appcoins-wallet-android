package com.asfoundation.wallet.topup

import cm.aptoide.analytics.AnalyticsManager

class TopUpAnalytics(private val analyticsManager: AnalyticsManager) {

  fun sendStartEvent() {
    analyticsManager.logEvent(HashMap<String, Any>(), WALLET_TOP_UP_START,
        AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendSelectionEvent(value: Double, action: String, paymentMethod: String) {
    val map = topUpBaseMap(value, paymentMethod)

    map[ACTION] = action

    analyticsManager.logEvent(map, WALLET_TOP_UP_SELECTION, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendConfirmationEvent(value: Double, action: String, paymentMethod: String) {
    val map = topUpBaseMap(value, paymentMethod)

    map[ACTION] = action

    analyticsManager.logEvent(map, WALLET_TOP_UP_CONFIRMATION, AnalyticsManager.Action.CLICK,
        WALLET)
  }

  fun sendSuccessEvent(value: Double, paymentMethod: String, status: String) {
    val map = topUpBaseMap(value, paymentMethod)

    map[STATUS] = status

    analyticsManager.logEvent(map, WALLET_TOP_UP_CONCLUSION, AnalyticsManager.Action.CLICK,
        WALLET)
  }

  fun sendErrorEvent(value: Double, paymentMethod: String, status: String,
                     errorCode: String, errorDetails: String) {
    val map = topUpBaseMap(value, paymentMethod)

    map[STATUS] = status
    map[ERROR_CODE] = errorCode
    map[ERROR_DETAILS] = errorDetails

    analyticsManager.logEvent(map, WALLET_TOP_UP_CONCLUSION, AnalyticsManager.Action.CLICK,
        WALLET)
  }

  private fun topUpBaseMap(value: Double, paymentMethod: String): HashMap<String, Any> {
    val map = HashMap<String, Any>()

    map[VALUE] = value
    map[METHOD] = paymentMethod

    return map
  }

  companion object {
    const val WALLET_TOP_UP_START = "wallet_top_up_start"
    const val WALLET_TOP_UP_SELECTION = "wallet_top_up_selection"
    const val WALLET_TOP_UP_CONFIRMATION = "wallet_top_up_confirmation"
    const val WALLET_TOP_UP_CONCLUSION = "wallet_top_up_conclusion"
    private const val VALUE = "value"
    private const val ACTION = "action"
    private const val METHOD = "method"
    private const val STATUS = "status"
    private const val ERROR_CODE = "error_code"
    private const val ERROR_DETAILS = "error_details"
    private const val WALLET = "wallet"
  }
}