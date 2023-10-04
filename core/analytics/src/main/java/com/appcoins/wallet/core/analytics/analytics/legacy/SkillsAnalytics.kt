package com.appcoins.wallet.core.analytics.analytics.legacy

import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData
import javax.inject.Inject

class SkillsAnalytics @Inject constructor(private val analytics: AnalyticsManager){

  private fun onboardingEventData(eskillsPaymentData: EskillsPaymentData): MutableMap<String, Any?> {
    val eventData: MutableMap<String, Any?> = HashMap()
    val purchaseData: MutableMap<String, Any?> = HashMap()
    purchaseData[EVENT_PACKAGE_NAME] = eskillsPaymentData.packageName
    purchaseData[EVENT_SKU] = eskillsPaymentData.product
    eventData[EVENT_PURCHASE] = purchaseData
    return eventData
  }

  private fun paymentEventData(eskillsPaymentData: EskillsPaymentData): MutableMap<String, Any?> {
    val eventData: MutableMap<String, Any?> = HashMap()
    val purchaseData: MutableMap<String, Any?> = HashMap()
    purchaseData[EVENT_PACKAGE_NAME] = eskillsPaymentData.packageName
    purchaseData[EVENT_SKU] = eskillsPaymentData.product
    purchaseData[EVENT_VALUE] = eskillsPaymentData.price
    purchaseData[EVENT_CURRENCY] = eskillsPaymentData.currency
    purchaseData[EVENT_MATCH_ENVIRONMENT] = eskillsPaymentData.environment
    purchaseData[QUEUE_ID] = eskillsPaymentData.queueId
    purchaseData[NUMBER_OF_USERS] = eskillsPaymentData.numberOfUsers
    eventData[EVENT_PURCHASE] = purchaseData
    return eventData
  }

  private fun pageViewEventData(eskillsPaymentData: EskillsPaymentData, context: String): MutableMap<String, Any?> {
    val eventData: MutableMap<String, Any?> = paymentEventData(eskillsPaymentData)
    eventData[CONTEXT] = context
    return eventData
  }

  private fun completedEventData(eventData: MutableMap<String, Any?> , type: String): MutableMap<String, Any?> {
    eventData[TYPE] = type
    return eventData
  }

  fun sendOnboardingLaunchEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(pageViewEventData(eskillsPaymentData,"EskillsOnboardingFragment"), WALLET_PAGE_VIEW ,AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendOnboardingCancelEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(
      completedEventData(onboardingEventData(eskillsPaymentData), ESKILLS_ONBOARDING_CANCEL),
      ESKILLS_ONBOARDING_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendOnboardingSuccessEvent(
    eskillsPaymentData: EskillsPaymentData,
    referralCode: String? = null
  ){
    val eventData = completedEventData(onboardingEventData(eskillsPaymentData), ESKILLS_ONBOARDING_SUCCESS)
    eventData[REFERRAL_CODE] = referralCode
    analytics.logEvent(
      eventData,
      ESKILLS_ONBOARDING_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendPaymentLaunchEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(pageViewEventData(eskillsPaymentData,"EskillsPaymentFragment"), WALLET_PAGE_VIEW ,AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendPaymentQueueIdInputEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_QUEUE_ID_INPUT, AnalyticsManager.Action.INPUT, WALLET)
  }

  fun sendPaymentTopUpErrorEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_TOPUP_ERROR, AnalyticsManager.Action.CLICK, WALLET)
  }
  fun sendPaymentNotSupportedErrorEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_NOT_SUPPORTED_ERROR, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendPaymentNoFundsErrorEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_NO_FUNDS_ERROR, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendPaymentBuyClickEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_BUY_CLICK, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendPaymentSuccessEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_SUCCESS),
      ESKILLS_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendPaymentCancelEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_CANCEL),
      ESKILLS_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendPaymentFailEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_ERROR),
      ESKILLS_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendPaymentGeoErrorEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_GEO_ERROR),
      ESKILLS_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendPaymentRootErrorEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_ROOT_ERROR),
      ESKILLS_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendPaymentVpnErrorEvent(eskillsPaymentData: EskillsPaymentData) {
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_VPN_ERROR),
      ESKILLS_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendPaymentWalletVersionErrorEvent(eskillsPaymentData: EskillsPaymentData) {
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_PAYMENT_WALLET_VERSION_ERROR),
      ESKILLS_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendPaymentCreateTicketFailError(eSkillsPaymentData: EskillsPaymentData) {
    analytics.logEvent(
      completedEventData(paymentEventData(eSkillsPaymentData), ESKILLS_PAYMENT_CREATE_TICKET_ERROR),
      ESKILLS_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendMatchmakingLaunchEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(pageViewEventData(eskillsPaymentData,"EskillsMatchmakingFragment"), WALLET_PAGE_VIEW ,AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendMatchmakingCancelEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_MATCHMAKING_CANCEL),
      ESKILLS_MATCHMAKING_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  fun sendMatchmakingCompletedEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_MATCHMAKING_SUCCESS),
      ESKILLS_MATCHMAKING_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )  }

  fun sendMatchmakingErrorEvent(
    eskillsPaymentData: EskillsPaymentData
  ){
    analytics.logEvent(
      completedEventData(paymentEventData(eskillsPaymentData), ESKILLS_MATCHMAKING_ERROR),
      ESKILLS_MATCHMAKING_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )  }

  fun sendReferralShareIntentionEvent(eSkillsPaymentData: EskillsPaymentData) {
    analytics.logEvent(paymentEventData(eSkillsPaymentData), ESKILLS_REFERRAL_SHARE_CLICK, AnalyticsManager.Action.CLICK, WALLET)
  }


  companion object {
    const val ESKILLS_ONBOARDING_CONCLUSION = "eskills_onboarding_conclusion"

    const val ESKILLS_ONBOARDING_CANCEL = "eskills_onboarding_cancel"
    const val ESKILLS_ONBOARDING_SUCCESS = "eskills_onboarding_success"

    const val ESKILLS_PAYMENT_QUEUE_ID_INPUT = "eskills_payment_queue_id_input"
    const val ESKILLS_PAYMENT_TOPUP_ERROR = "eskills_topup_required_error"
    const val ESKILLS_PAYMENT_NO_FUNDS_ERROR = "eskills_topup_required_error"
    const val ESKILLS_PAYMENT_BUY_CLICK = "eskills_payment_buy_click"

    const val ESKILLS_PAYMENT_CONCLUSION = "eskills_payment_conclusion"

    const val ESKILLS_PAYMENT_SUCCESS = "eskills_payment_success"
    const val ESKILLS_PAYMENT_VPN_ERROR = "eskills_payment_vpn_error"
    const val ESKILLS_PAYMENT_WALLET_VERSION_ERROR = "eskills_payment_wallet_version_error"
    const val ESKILLS_PAYMENT_CREATE_TICKET_ERROR = "eskills_payment_create_ticket_fail_error"
    const val ESKILLS_PAYMENT_CANCEL = "eskills_payment_cancel"
    const val ESKILLS_PAYMENT_ERROR = "eskills_payment_fail_error"
    const val ESKILLS_PAYMENT_GEO_ERROR = "eskills_payment_geo_error"
    const val ESKILLS_PAYMENT_ROOT_ERROR = "eskills_payment_root_error"
    const val ESKILLS_PAYMENT_NOT_SUPPORTED_ERROR = "eskills_payment_method_not_supported_error"

    const val ESKILLS_MATCHMAKING_CONCLUSION = "eskills_matchmaking_conclusion"

    const val ESKILLS_MATCHMAKING_CANCEL = "eskills_matchmaking_cancel"
    const val ESKILLS_MATCHMAKING_ERROR = "eskills_matchmaking_error"
    const val ESKILLS_MATCHMAKING_SUCCESS = "eskills_matchmaking_success"

    const val ESKILLS_REFERRAL_SHARE_CLICK = "eskills_referral_share_click"

    const val WALLET_PAGE_VIEW = "wallet_page_view"

    private const val WALLET = "WALLET"
    private const val TYPE = "TYPE"
    private const val CONTEXT = "context"
    private const val EVENT_PACKAGE_NAME = "package_name"
    private const val EVENT_SKU = "sku"
    private const val EVENT_VALUE = "value"
    private const val EVENT_CURRENCY = "currency"
    private const val EVENT_PURCHASE = "purchase"
    private const val EVENT_MATCH_ENVIRONMENT = "match_environment"
    private const val NUMBER_OF_USERS = "number_of_users"
    private const val QUEUE_ID = "queue_id"
    private const val REFERRAL_CODE = "queue_id"
  }

}