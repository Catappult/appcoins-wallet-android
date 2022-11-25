package com.asfoundation.wallet.topup

import cm.aptoide.analytics.AnalyticsManager
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import javax.inject.Inject

class TopUpAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  fun sendStartEvent() {
    analyticsManager.logEvent(
      HashMap<String, Any>(), WALLET_TOP_UP_START,
      AnalyticsManager.Action.CLICK, WALLET
    )
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

  fun sendPaypalUrlEvent(value: Double, paymentMethod: String, type: String?, resultCode: String?,
                         url: String) {
    val map = topUpBaseMap(value, paymentMethod)

    type?.let { map[PAYPAL_TYPE] = it }
    resultCode?.let { map[RESULT_CODE] = it }
    if (url.length > MAX_CHARACTERS) {
      map[URL] = url.takeLast(MAX_CHARACTERS)
    } else {
      map[URL] = url
    }

    analyticsManager.logEvent(map, WALLET_TOP_UP_PAYPAL_URL, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendErrorEvent(value: Double, paymentMethod: String, status: String,
                     errorCode: String, errorDetails: String, errorRiskRules: String? = null) {
    val map = topUpBaseMap(value, paymentMethod)

    map[STATUS] = status
    map[ERROR_CODE] = errorCode
    map[ERROR_DETAILS] = errorDetails
    if (errorRiskRules != null) map[ERROR_CODE_RISK_RULE] = errorRiskRules

    analyticsManager.logEvent(map, WALLET_TOP_UP_CONCLUSION, AnalyticsManager.Action.CLICK,
        WALLET)
  }

  fun sendPaypalSuccessEvent(value: String) {
    val map = HashMap<String, Any>()
    map[METHOD] = PaymentMethodsAnalytics.PAYMENT_METHOD_PP_V2
    map[VALUE] = value
    map[STATUS] = STATUS_SUCCESS
    analyticsManager.logEvent(map, WALLET_TOP_UP_CONCLUSION, AnalyticsManager.Action.CLICK,
      WALLET)
  }

  fun sendPaypalErrorEvent(errorDetails: String) {
    val map = HashMap<String, Any>()
    map[METHOD] = PaymentMethodsAnalytics.PAYMENT_METHOD_PP_V2
    map[ERROR_DETAILS] = errorDetails
    analyticsManager.logEvent(map, WALLET_TOP_UP_CONCLUSION, AnalyticsManager.Action.CLICK,
      WALLET)
  }

  fun sendBillingAddressActionEvent(value: Double,
                                    paymentMethod: String,
                                    action: String) {
    val map = topUpBaseMap(value, paymentMethod)

    map[ACTION] = action

    analyticsManager.logEvent(map, RAKAM_TOP_UP_BILLING, AnalyticsManager.Action.CLICK,
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
    const val WALLET_TOP_UP_PAYPAL_URL = "wallet_top_up_conclusion_paypal"
    const val RAKAM_TOP_UP_BILLING = "wallet_top_up_billing"
    const val STATUS_SUCCESS = "success"
    private const val VALUE = "value"
    private const val ACTION = "action"
    private const val METHOD = "payment_method"
    private const val STATUS = "status"
    private const val ERROR_CODE = "error_code"
    private const val ERROR_DETAILS = "error_details"
    private const val ERROR_CODE_RISK_RULE = "error_code_risk_rule"
    private const val PAYPAL_TYPE = "type"
    private const val RESULT_CODE = "result_code"
    private const val URL = "url"
    private const val WALLET = "wallet"
    private const val MAX_CHARACTERS = 100
  }
}