package com.asfoundation.wallet.wallet_validation.generic

import cm.aptoide.analytics.AnalyticsManager
import java.util.*

class WalletValidationAnalytics(private val analyticsManager: AnalyticsManager) {

  fun sendPhoneVerificationEvent(action: String, context: String, status: String, error: String) {
    val eventData = buildBaseDataMap(status, error)

    eventData[ACTION] = action
    eventData[CONTEXT] = context

    analyticsManager.logEvent(eventData, WALLET_PHONE_NUMBER_VERIFICATION,
        AnalyticsManager.Action.CLICK,
        WALLET)
  }

  fun sendCodeVerificationEvent(action: String) {
    val eventData = HashMap<String, Any>()

    eventData[ACTION] = action

    analyticsManager.logEvent(eventData, WALLET_CODE_VERIFICATION, AnalyticsManager.Action.CLICK,
        WALLET)
  }


  fun sendConfirmationEvent(status: String, error: String) {
    val eventData = buildBaseDataMap(status, error)

    analyticsManager.logEvent(eventData, WALLET_VERIFICATION_CONFIRMATION,
        AnalyticsManager.Action.CLICK,
        WALLET)
  }


  private fun buildBaseDataMap(status: String, error: String): HashMap<String, Any> {
    val eventData = HashMap<String, Any>()

    eventData[STATUS] = status
    if (error.isNotEmpty()) eventData[ERROR_DETAILS] = error

    return eventData
  }

  companion object {
    const val WALLET_PHONE_NUMBER_VERIFICATION = "wallet_phone_number_verification"
    const val WALLET_CODE_VERIFICATION = "wallet_code_verification"
    const val WALLET_VERIFICATION_CONFIRMATION = "wallet_verification_confirmation"

    private const val ACTION = "action"
    private const val STATUS = "status"
    private const val CONTEXT = "context"
    private const val ERROR_DETAILS = "error_details"

    private const val WALLET = "wallet"
  }
}