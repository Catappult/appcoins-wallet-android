package com.appcoins.wallet.core.analytics.analytics.legacy

import android.content.Context
import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.analytics.analytics.gameshub.GamesHubBroadcastService
import com.appcoins.wallet.core.analytics.analytics.partners.GamesHubContentProviderService
import com.appcoins.wallet.sharedpreferences.AppStartPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.OemIdPreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = EventSender::class)
class BillingAnalytics @Inject constructor(
  private val analytics: AnalyticsManager,
  @ApplicationContext private val context: Context,
  private val oemIdPreferencesDataSource: OemIdPreferencesDataSource,
  private val appStartPreferencesDataSource: AppStartPreferencesDataSource,
  private val gamesHubContentProviderService: GamesHubContentProviderService,
) : EventSender {

  override fun sendPurchaseDetailsEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    transactionType: String?,
    isOnboardingPayment: Boolean
  ) {
    val purchaseData = mutableMapOf(
      EVENT_PACKAGE_NAME to packageName,
      EVENT_SKU to skuDetails,
      EVENT_VALUE to value,
    )
    val eventData = mutableMapOf(
      EVENT_PURCHASE to purchaseData,
      EVENT_TRANSACTION_TYPE to transactionType,
    ).apply { if (isOnboardingPayment) this[EVENT_ONBOARDING_PAYMENT] = true }

    analytics.logEvent(
      eventData,
      PURCHASE_DETAILS,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPaymentMethodDetailsEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    isOnboardingPayment: Boolean
  ) {
    val purchaseData = mutableMapOf(
      EVENT_PACKAGE_NAME to packageName,
      EVENT_SKU to skuDetails,
      EVENT_VALUE to value,
    )
    val eventData = mutableMapOf(
      EVENT_PURCHASE to purchaseData,
      EVENT_PAYMENT_METHOD to purchaseDetails,
      EVENT_TRANSACTION_TYPE to transactionType,
    ).apply { if (isOnboardingPayment) this[EVENT_ONBOARDING_PAYMENT] = true }

    analytics.logEvent(
      eventData,
      PAYMENT_METHOD_DETAILS,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
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
      packageName = packageName,
      skuDetails = skuDetails,
      value = value,
      purchaseDetails = purchaseDetails,
      transactionType = transactionType,
      action = action,
      isOnboardingPayment = isOnboardingPayment
    )
    analytics.logEvent(
      eventData,
      WALLET_PAYMENT_METHOD_DETAILS,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPaymentEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    isOnboardingPayment: Boolean
  ) {
    val purchaseData = mutableMapOf(
      EVENT_PACKAGE_NAME to packageName,
      EVENT_VALUE to value,
    ).apply { skuDetails?.let { this[EVENT_SKU] = skuDetails } }

    val eventData = mutableMapOf(
      EVENT_PURCHASE to purchaseData,
      EVENT_PAYMENT_METHOD to purchaseDetails,
      EVENT_TRANSACTION_TYPE to transactionType,
    ).apply { if (isOnboardingPayment) this[EVENT_ONBOARDING_PAYMENT] = true }

    analytics.logEvent(
      eventData,
      PAYMENT,
      AnalyticsManager.Action.IMPRESSION,
      WALLET
    )
  }

  override fun sendRevenueEvent(
    value: String,
    isOnboardingPayment: Boolean
  ) {
    val eventData = mutableMapOf<String, Any>(
      EVENT_VALUE to value,
      EVENT_OEMID to oemIdPreferencesDataSource.getCurrentOemId(),
    ).apply { if (isOnboardingPayment) this[EVENT_ONBOARDING_PAYMENT] = true }

    analytics.logEvent(
      eventData,
      REVENUE,
      AnalyticsManager.Action.IMPRESSION,
      WALLET
    )
  }

  override fun sendPreSelectedPaymentMethodEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String?,
    action: String,
    isOnboardingPayment: Boolean
  ) {
    val eventData = createBaseWalletEventMap(
      packageName = packageName,
      skuDetails = skuDetails,
      value = value,
      purchaseDetails = purchaseDetails,
      transactionType = transactionType,
      action = action,
      isOnboardingPayment = isOnboardingPayment
    ).apply { this[EVENT_OEMID] = oemIdPreferencesDataSource.getCurrentOemId() }

    analytics.logEvent(
      eventData,
      WALLET_PRESELECTED_PAYMENT_METHOD,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPaymentMethodEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String?,
    action: String,
    isOnboardingPayment: Boolean
  ) {
    val eventData = createBaseWalletEventMap(
      packageName = packageName,
      skuDetails = skuDetails,
      value = value,
      purchaseDetails = purchaseDetails,
      transactionType = transactionType,
      action = action,
      isOnboardingPayment = isOnboardingPayment
    ).apply { this[EVENT_OEMID] = oemIdPreferencesDataSource.getCurrentOemId() }

    analytics.logEvent(
      eventData,
      WALLET_PAYMENT_METHOD,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPaymentConfirmationEvent(
    packageName: String?,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String?,
    action: String,
    isOnboardingPayment: Boolean
  ) {
    val eventData = createBaseWalletEventMap(
      packageName = packageName,
      skuDetails = skuDetails,
      value = value,
      purchaseDetails = purchaseDetails,
      transactionType = transactionType,
      action = action,
      isOnboardingPayment = isOnboardingPayment
    ).apply { this[EVENT_OEMID] = oemIdPreferencesDataSource.getCurrentOemId() }

    analytics.logEvent(
      eventData,
      WALLET_PAYMENT_CONFIRMATION,
      AnalyticsManager.Action.CLICK,
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
      packageName = packageName,
      skuDetails = skuDetails,
      value = value,
      purchaseDetails = purchaseDetails,
      transactionType = transactionType,
      status = EVENT_FAIL,
      isOnboardingPayment = isOnboardingPayment
    ).apply {
      this[EVENT_OEMID] = oemIdPreferencesDataSource.getCurrentOemId()
      this[EVENT_ERROR_CODE] = errorCode
    }
    analytics.logEvent(
      eventData,
      WALLET_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
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
      packageName = packageName,
      skuDetails = skuDetails,
      value = value,
      purchaseDetails = purchaseDetails,
      transactionType = transactionType,
      status = EVENT_FAIL,
      isOnboardingPayment = isOnboardingPayment
    ).apply {
      this[EVENT_OEMID] = oemIdPreferencesDataSource.getCurrentOemId()
      this[EVENT_ERROR_CODE] = errorCode
      this[EVENT_ERROR_DETAILS] = errorDetails
    }

    analytics.logEvent(
      eventData,
      WALLET_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPaymentErrorWithDetailsAndRiskEvent(
    packageName: String,
    skuDetails: String,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    errorCode: String,
    errorDetails: String?,
    riskRules: String?,
    isOnboardingPayment: Boolean,
    isWebViewPayment:Boolean,
  ) {
    val eventData = createConclusionWalletEventMap(
      packageName = packageName,
      skuDetails = skuDetails,
      value = value,
      purchaseDetails = purchaseDetails,
      transactionType = transactionType,
      status = EVENT_FAIL,
      isOnboardingPayment = isOnboardingPayment
    ).apply {
      this[EVENT_OEMID] = oemIdPreferencesDataSource.getCurrentOemId()
      this[EVENT_ERROR_CODE] = errorCode
      errorDetails?.let { this[EVENT_ERROR_DETAILS] = it }
      riskRules?.let { this[EVENT_CODE_RISK_RULES] = it }
      this[EVENT_PAYMENT_CHANNEL] = if (isWebViewPayment) WEBVIEW else APPCOINS_WALLET
    }

    analytics.logEvent(
      eventData,
      WALLET_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPaymentSuccessEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    isOnboardingPayment: Boolean,
    txId: String,
    valueUsd: String,
    isStoredCard: Boolean?,
    wasCvcRequired: Boolean?,
    isWebViewPayment:Boolean,
  ) {
    val eventData: MutableMap<String, Any?> = createConclusionWalletEventMap(
      packageName = packageName,
      skuDetails = skuDetails,
      value = value,
      purchaseDetails = purchaseDetails,
      transactionType = transactionType,
      status = EVENT_SUCCESS,
      isOnboardingPayment = isOnboardingPayment,
      cardPaymentType = when {
        isStoredCard == true && wasCvcRequired == true -> EVENT_STORED_CARD_CVC_REQUIRED
        isStoredCard == true && wasCvcRequired == false -> EVENT_STORED_CARD_CVC_NOT_REQUIRED
        isStoredCard == false -> EVENT_NEW_CARD
        else -> null
      }
    )

    // The broadcast is only sent when there's an older versions of GamesHub installed,
    // without contentProvider.
    if (!gamesHubContentProviderService.doesProviderExist()) {
      GamesHubBroadcastService.sendSuccessPaymentBroadcast(
        context = context,
        uid = txId,
        packageName = packageName,
        usdAmount = valueUsd,
        appcAmount = value
      )
    }

    eventData[EVENT_OEMID] = oemIdPreferencesDataSource.getCurrentOemId()
    eventData[EVENT_PAYMENT_CHANNEL] = if (isWebViewPayment) WEBVIEW else APPCOINS_WALLET
    analytics.logEvent(
      eventData,
      WALLET_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
    appStartPreferencesDataSource.saveIsFirstPayment(isFirstPayment = false)
  }

  override fun sendPaymentPendingEvent(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    isOnboardingPayment: Boolean
  ) {
    val eventData: MutableMap<String, Any?> = createConclusionWalletEventMap(
      packageName = packageName,
      skuDetails = skuDetails,
      value = value,
      purchaseDetails = purchaseDetails,
      transactionType = transactionType,
      status = EVENT_PENDING,
      isOnboardingPayment = isOnboardingPayment
    ).apply { this[EVENT_OEMID] = oemIdPreferencesDataSource.getCurrentOemId() }

    analytics.logEvent(
      eventData,
      WALLET_PAYMENT_CONCLUSION,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  override fun sendPurchaseStartEvent(
    packageName: String?,
    skuDetails: String?,
    value: String,
    transactionType: String?,
    context: String,
    oemId: String?,
    purchaseDetails: String?,
    isOnboardingPayment: Boolean,
    isWebViewPayment: Boolean,
  ) {
    val eventData = mutableMapOf<String, Any?>(
      EVENT_PACKAGE_NAME to packageName,
      EVENT_SKU to skuDetails,
      EVENT_VALUE to value,
      EVENT_TRANSACTION_TYPE to transactionType,
      EVENT_CONTEXT to context,
      EVENT_OEMID to (oemId ?: ""),
      EVENT_PAYMENT_CHANNEL to if (isWebViewPayment) WEBVIEW else APPCOINS_WALLET,
    ).apply {
      purchaseDetails?.let { this[EVENT_PAYMENT_METHOD] = it }
      if (isOnboardingPayment) this[EVENT_ONBOARDING_PAYMENT] = true
    }

    analytics.logEvent(
      eventData,
      WALLET_PAYMENT_START,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
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
    val eventData = mutableMapOf<String, Any?>(
      EVENT_PACKAGE_NAME to packageName,
      EVENT_SKU to skuDetails,
      EVENT_VALUE to value,
      EVENT_TRANSACTION_TYPE to transactionType,
      EVENT_PAYPAL_TYPE to type,
      EVENT_RESULT_CODE to resultCode,
      EVENT_OEMID to oemIdPreferencesDataSource.getCurrentOemId(),
    ).apply {
      this[EVENT_URL] = if (url != null && url.length > MAX_CHARACTERS) {
        url.substring(url.length - MAX_CHARACTERS)
      } else {
        url
      }
      if (isOnboardingPayment) this[EVENT_ONBOARDING_PAYMENT] = true
    }

    analytics.logEvent(
      eventData,
      WALLET_PAYPAL_URL,
      AnalyticsManager.Action.CLICK,
      WALLET
    )
  }

  private fun createBaseWalletEventMap(
    packageName: String?,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String?,
    action: String,
    isOnboardingPayment: Boolean = false
  ): MutableMap<String, Any?> = mutableMapOf<String, Any?>(
    EVENT_PACKAGE_NAME to packageName,
    EVENT_SKU to skuDetails,
    EVENT_VALUE to value,
    EVENT_TRANSACTION_TYPE to transactionType,
    EVENT_PAYMENT_METHOD to purchaseDetails,
    EVENT_ACTION to action,
  ).apply { if (isOnboardingPayment) this[EVENT_ONBOARDING_PAYMENT] = true }

  private fun createConclusionWalletEventMap(
    packageName: String,
    skuDetails: String?,
    value: String,
    purchaseDetails: String,
    transactionType: String,
    status: String,
    isOnboardingPayment: Boolean = false,
    cardPaymentType: String? = null,
  ): MutableMap<String, Any?> = mutableMapOf<String, Any?>(
    EVENT_PACKAGE_NAME to packageName,
    EVENT_SKU to skuDetails,
    EVENT_VALUE to value,
    EVENT_TRANSACTION_TYPE to transactionType,
    EVENT_PAYMENT_METHOD to purchaseDetails,
    EVENT_STATUS to status,
    EVENT_CARD_PAYMENT_TYPE to cardPaymentType,
  ).apply { if (isOnboardingPayment) this[EVENT_ONBOARDING_PAYMENT] = true }

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
    const val PAYMENT_METHOD_GOOGLE_PAY_WEB = "GOOGLE_PAY"
    const val PAYMENT_METHOD_VK_PAY = "VK_PAY"
    const val PAYMENT_METHOD_CARRIER = "CARRIER"
    const val PAYMENT_METHOD_SANDBOX = "SANDBOX"
    const val PAYMENT_METHOD_MI_PAY = "MI_PAY"
    const val PAYMENT_METHOD_AMAZON_PAY = "AMAZON_PAY"
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
    private const val EVENT_OEMID = "oem_id"
    private const val EVENT_CARD_PAYMENT_TYPE = "card_payment_type"
    private const val MAX_CHARACTERS = 100
    private const val EVENT_STORED_CARD_CVC_REQUIRED = "stored_card_cvc_required"
    private const val EVENT_STORED_CARD_CVC_NOT_REQUIRED = "stored_card_cvc_not_required"
    private const val EVENT_NEW_CARD = "new_card"
    private const val EVENT_PAYMENT_CHANNEL = "payment_channel"
    private const val WEBVIEW = "appcoins_wallet_android_webview"
    private const val APPCOINS_WALLET = "appcoins_wallet_android"
    const val ACTION_BUY = "buy"
    const val ACTION_NEXT = "next"
    const val ACTION_CANCEL = "cancel"
    const val ACTION_BACK = "back"
  }

}
