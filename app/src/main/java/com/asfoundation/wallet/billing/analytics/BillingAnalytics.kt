package com.asfoundation.wallet.billing.analytics

import cm.aptoide.analytics.AnalyticsManager
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = EventSender::class)
class BillingAnalytics @Inject constructor(private val analytics: AnalyticsManager) : EventSender {
  override fun sendPurchaseDetailsEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    transactionType: String?,
    isOnboardingPayment: Boolean
  ) {
    val eventData: MutableMap<String, Any?> = HashMap()
    val purchaseData: MutableMap<String, Any?> = HashMap()
    purchaseData[EVENT_PACKAGE_NAME] = packageName
    purchaseData[EVENT_SKU] = skuDetails
    purchaseData[EVENT_VALUE] = value
    eventData[EVENT_PURCHASE] = purchaseData
    eventData[EVENT_TRANSACTION_TYPE] = transactionType
    if (isOnboardingPayment) eventData[EVENT_ONBOARDING_PAYMENT] = true
    analytics.logEvent(eventData, PURCHASE_DETAILS, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendPaymentMethodDetailsEvent(
    packageName: String, skuDetails: String?, value: String,
    purchaseDetails: String, transactionType: String, isOnboardingPayment: Boolean
  ) {
    val eventData: MutableMap<String, Any?> = HashMap()
    val purchaseData: MutableMap<String, Any?> = HashMap()
    purchaseData[EVENT_PACKAGE_NAME] = packageName
    purchaseData[EVENT_SKU] = skuDetails
    purchaseData[EVENT_VALUE] = value
    eventData[EVENT_PURCHASE] = purchaseData
    eventData[EVENT_PAYMENT_METHOD] = purchaseDetails
    eventData[EVENT_TRANSACTION_TYPE] = transactionType
    if (isOnboardingPayment) eventData[EVENT_ONBOARDING_PAYMENT] = true
    analytics.logEvent(eventData, PAYMENT_METHOD_DETAILS, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendActionPaymentMethodDetailsActionEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    action: String,
    isOnboardingPayment: Boolean
  ) {
    val eventData = createBaseWalletEventMap(
      packageName, skuDetails, value, purchaseDetails, transactionType,
      action, isOnboardingPayment
    )
    analytics.logEvent(
      eventData, WALLET_PAYMENT_METHOD_DETAILS, AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPaymentEvent(
    packageName: String, skuDetails: String?, value: String,
    purchaseDetails: String, transactionType: String, isOnboardingPayment: Boolean
  ) {
    val eventData: MutableMap<String, Any> = HashMap()
    val purchaseData: MutableMap<String, Any> = HashMap()
    purchaseData[EVENT_PACKAGE_NAME] = packageName
    skuDetails?.let { purchaseData[EVENT_SKU] = skuDetails }
    purchaseData[EVENT_VALUE] = value
    eventData[EVENT_PURCHASE] = purchaseData
    eventData[EVENT_PAYMENT_METHOD] = purchaseDetails
    eventData[EVENT_TRANSACTION_TYPE] = transactionType
    if (isOnboardingPayment) eventData[EVENT_ONBOARDING_PAYMENT] = true
    analytics.logEvent(eventData, PAYMENT, AnalyticsManager.Action.IMPRESSION, WALLET)
  }

  override fun sendRevenueEvent(value: String, isOnboardingPayment: Boolean) {
    val eventData: MutableMap<String, Any> = HashMap()
    eventData[EVENT_VALUE] = value
    if (isOnboardingPayment) eventData[EVENT_ONBOARDING_PAYMENT] =
      true
    analytics.logEvent(eventData, REVENUE, AnalyticsManager.Action.IMPRESSION, WALLET)
  }

  override fun sendPreSelectedPaymentMethodEvent(
    packageName: String, skuDetails: String?, value: String,
    purchaseDetails: String, transactionType: String?, action: String, isOnboardingPayment: Boolean
  ) {
    val eventData = createBaseWalletEventMap(
      packageName, skuDetails, value, purchaseDetails, transactionType,
      action, isOnboardingPayment
    )
    analytics.logEvent(
      eventData, WALLET_PRESELECTED_PAYMENT_METHOD, AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPaymentMethodEvent(
    packageName: String, skuDetails: String?, value: String,
    purchaseDetails: String, transactionType: String?, action: String, isOnboardingPayment: Boolean
  ) {
    val eventData = createBaseWalletEventMap(
      packageName, skuDetails, value, purchaseDetails, transactionType,
      action, isOnboardingPayment
    )
    analytics.logEvent(eventData, WALLET_PAYMENT_METHOD, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendPaymentConfirmationEvent(
    packageName: String?, skuDetails: String?, value: String,
    purchaseDetails: String, transactionType: String?, action: String, isOnboardingPayment: Boolean
  ) {
    val eventData = createBaseWalletEventMap(
      packageName, skuDetails, value, purchaseDetails, transactionType,
      action, isOnboardingPayment
    )
    analytics.logEvent(
      eventData, WALLET_PAYMENT_CONFIRMATION, AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPaymentErrorEvent(
    packageName: String,
    skuDetails: String,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    errorCode: String,
    isOnboardingPayment: Boolean
  ) {
    val eventData = createConclusionWalletEventMap(
      packageName, skuDetails, value, purchaseDetails,
      transactionType, EVENT_FAIL, isOnboardingPayment
    )
    eventData[EVENT_ERROR_CODE] = errorCode
    analytics.logEvent(eventData, WALLET_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendPaymentErrorWithDetailsEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    errorCode: String,
    errorDetails: String?,
    isOnboardingPayment: Boolean
  ) {
    val eventData = createConclusionWalletEventMap(
      packageName, skuDetails, value, purchaseDetails,
      transactionType, EVENT_FAIL, isOnboardingPayment
    )
    eventData[EVENT_ERROR_CODE] = errorCode
    eventData[EVENT_ERROR_DETAILS] = errorDetails
    analytics.logEvent(eventData, WALLET_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendPaymentErrorWithDetailsAndRiskEvent(
    packageName: String, skuDetails: String,
    value: String, purchaseDetails: String, transactionType: String, errorCode: String,
    errorDetails: String?, riskRules: String?, isOnboardingPayment: Boolean
  ) {
    val eventData = createConclusionWalletEventMap(
      packageName, skuDetails, value, purchaseDetails,
      transactionType, EVENT_FAIL, isOnboardingPayment
    )
    eventData[EVENT_ERROR_CODE] = errorCode
    errorDetails?.let { eventData[EVENT_ERROR_DETAILS] = errorDetails }
    riskRules?.let { eventData[EVENT_CODE_RISK_RULES] = riskRules }
    analytics.logEvent(eventData, WALLET_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendPaymentSuccessEvent(
    packageName: String, skuDetails: String?, value: String,
    purchaseDetails: String, transactionType: String, isOnboardingPayment: Boolean
  ) {
    val eventData: Map<String, Any?> = createConclusionWalletEventMap(
      packageName, skuDetails, value, purchaseDetails,
      transactionType, EVENT_SUCCESS, isOnboardingPayment
    )
    analytics.logEvent(eventData, WALLET_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendPaymentPendingEvent(
    packageName: String, skuDetails: String?, value: String,
    purchaseDetails: String, transactionType: String, isOnboardingPayment: Boolean
  ) {
    val eventData: Map<String, Any?> = createConclusionWalletEventMap(
      packageName, skuDetails, value, purchaseDetails,
      transactionType, EVENT_PENDING, isOnboardingPayment
    )
    analytics.logEvent(eventData, WALLET_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendPurchaseStartEvent(
    packageName: String?, skuDetails: String?, value: String,
    purchaseDetails: String, transactionType: String?, context: String, isOnboardingPayment: Boolean
  ) {
    val eventData: MutableMap<String, Any?> = HashMap()
    eventData[EVENT_PACKAGE_NAME] = packageName
    eventData[EVENT_SKU] = skuDetails
    eventData[EVENT_VALUE] = value
    eventData[EVENT_TRANSACTION_TYPE] = transactionType
    eventData[EVENT_PAYMENT_METHOD] = purchaseDetails
    eventData[EVENT_CONTEXT] = context
    if (isOnboardingPayment) eventData[EVENT_ONBOARDING_PAYMENT] = true
    analytics.logEvent(eventData, WALLET_PAYMENT_START, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendPurchaseStartWithoutDetailsEvent(
    packageName: String?, skuDetails: String?,
    value: String, transactionType: String?, context: String, isOnboardingPayment: Boolean
  ) {
    val eventData: MutableMap<String, Any?> = HashMap()
    eventData[EVENT_PACKAGE_NAME] = packageName
    eventData[EVENT_SKU] = skuDetails
    eventData[EVENT_VALUE] = value
    eventData[EVENT_TRANSACTION_TYPE] = transactionType
    eventData[EVENT_CONTEXT] = context
    if (isOnboardingPayment) eventData[EVENT_ONBOARDING_PAYMENT] = true
    analytics.logEvent(eventData, WALLET_PAYMENT_START, AnalyticsManager.Action.CLICK, WALLET)
  }

  override fun sendPaypalUrlEvent(
    packageName: String?,
    skuDetails: String?,
    value: String,
    transactionType: String,
    type: String?,
    resultCode: String?,
    url: String?,
    isOnboardingPayment: Boolean
  ) {
    val eventData: MutableMap<String, Any?> = HashMap()
    eventData[EVENT_PACKAGE_NAME] = packageName
    eventData[EVENT_SKU] = skuDetails
    eventData[EVENT_VALUE] = value
    eventData[EVENT_TRANSACTION_TYPE] = transactionType
    eventData[EVENT_PAYPAL_TYPE] = type
    eventData[EVENT_RESULT_CODE] = resultCode
    if (url?.length!! > MAX_CHARACTERS) {
      eventData[EVENT_URL] = url.substring(url.length - MAX_CHARACTERS)
    } else {
      eventData[EVENT_URL] = url
    }
    if (isOnboardingPayment) eventData[EVENT_ONBOARDING_PAYMENT] = true
    analytics.logEvent(eventData, WALLET_PAYPAL_URL, AnalyticsManager.Action.CLICK, WALLET)
  }

  fun sendBillingAddressActionEvent(
    packageName: String,
    skuDetails: String,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    action: String,
    isOnboardingPayment: Boolean = false
  ) {
    val eventData = createBaseWalletEventMap(
      packageName, skuDetails, value, purchaseDetails, transactionType,
      action, isOnboardingPayment
    )
    analytics.logEvent(eventData, WALLET_PAYMENT_BILLING, AnalyticsManager.Action.CLICK, WALLET)
  }

  private fun createBaseWalletEventMap(
    packageName: String?,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String?,
    action: String,
    isOnboardingPayment: Boolean = false
  ): Map<String, Any?> {
    val eventData: MutableMap<String, Any?> = HashMap()
    eventData[EVENT_PACKAGE_NAME] = packageName
    eventData[EVENT_SKU] = skuDetails
    eventData[EVENT_VALUE] = value
    eventData[EVENT_TRANSACTION_TYPE] = transactionType
    eventData[EVENT_PAYMENT_METHOD] = purchaseDetails
    eventData[EVENT_ACTION] = action
    if (isOnboardingPayment) eventData[EVENT_ONBOARDING_PAYMENT] = true
    return eventData
  }

  private fun createConclusionWalletEventMap(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    status: String,
    isOnboardingPayment: Boolean = false
  ): MutableMap<String, Any?> {
    val eventData: MutableMap<String, Any?> = HashMap()
    eventData[EVENT_PACKAGE_NAME] = packageName
    eventData[EVENT_SKU] = skuDetails
    eventData[EVENT_VALUE] = value
    eventData[EVENT_TRANSACTION_TYPE] = transactionType
    eventData[EVENT_PAYMENT_METHOD] = purchaseDetails
    eventData[EVENT_STATUS] = status
    if (isOnboardingPayment) eventData[EVENT_ONBOARDING_PAYMENT] = true
    return eventData
  }

  companion object {
    const val PURCHASE_DETAILS = "PURCHASE_DETAILS"
    const val PAYMENT_METHOD_DETAILS = "PAYMENT_METHOD_DETAILS"
    const val PAYMENT = "PAYMENT"
    const val REVENUE = "REVENUE"
    const val PAYMENT_METHOD_APPC = "APPC"
    const val PAYMENT_METHOD_CC = "CREDIT_CARD"
    const val PAYMENT_METHOD_REWARDS = "REWARDS"
    const val PAYMENT_METHOD_PAYPAL = "PAYPAL"
    const val PAYMENT_METHOD_PAYPALV2 = "PAYPAL_V2"
    const val PAYMENT_METHOD_CARRIER = "CARRIER"
    const val WALLET_PRESELECTED_PAYMENT_METHOD = "wallet_preselected_payment_method"
    const val WALLET_PAYMENT_METHOD = "wallet_payment_method"
    const val WALLET_PAYMENT_CONFIRMATION = "wallet_payment_confirmation"
    const val WALLET_PAYMENT_CONCLUSION = "wallet_payment_conclusion"
    const val WALLET_PAYMENT_START = "wallet_payment_start"
    const val WALLET_PAYPAL_URL = "wallet_payment_conclusion_paypal"
    const val WALLET_PAYMENT_METHOD_DETAILS = "wallet_payment_method_details"
    const val WALLET_PAYMENT_BILLING = "wallet_payment_billing"
    const val EVENT_REVENUE_CURRENCY = "EUR"
    const val EVENT_ACTION = "action"
    private const val WALLET = "WALLET"
    private const val EVENT_PACKAGE_NAME = "package_name"
    private const val EVENT_SKU = "sku"
    private const val EVENT_VALUE = "value"
    private const val EVENT_PURCHASE = "purchase"
    private const val EVENT_TRANSACTION_TYPE = "transaction_type"
    private const val EVENT_PAYMENT_METHOD = "payment_method"
    private const val EVENT_CONTEXT = "context"
    private const val EVENT_STATUS = "status"
    private const val EVENT_ERROR_CODE = "error_code"
    private const val EVENT_ERROR_DETAILS = "error_details"
    private const val EVENT_CODE_RISK_RULES = "error_code_risk_rule"
    private const val EVENT_SUCCESS = "success"
    private const val EVENT_FAIL = "fail"
    private const val EVENT_PENDING = "pending"
    private const val EVENT_PAYPAL_TYPE = "type"
    private const val EVENT_RESULT_CODE = "result_code"
    private const val EVENT_URL = "url"
    private const val EVENT_ONBOARDING_PAYMENT = "onboarding_payment"
    private const val MAX_CHARACTERS = 100
  }

}