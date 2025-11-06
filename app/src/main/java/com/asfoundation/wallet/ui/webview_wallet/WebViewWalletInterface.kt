package com.asfoundation.wallet.ui.webview_wallet

import android.util.Log
import android.webkit.JavascriptInterface
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.ui.webview_payment.models.CloseBehaviorConfig
import com.asfoundation.wallet.ui.webview_payment.models.VerifyFlowWeb
import com.asfoundation.wallet.ui.webview_payment.models.WebPaymentSuccessParser
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentErrorResponse
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentResponse
import com.google.gson.Gson

class WebViewWalletInterface(
  private val logger: Logger,
  private val intercomCallback: () -> Unit,
  private val allowExternalAppsCallback: (allow: Boolean) -> Unit,
  private val onPurchaseResultCallback: (WebViewPaymentResponse?) -> Unit,
  private val onOpenDeepLink: (deepLink: String?) -> Unit,
  private val onStartExternalPayment: (deepLink: String?) -> Unit,
  private val onErrorCallback: (WebViewPaymentErrorResponse?) -> Unit,
  private val openVerifyFlowCallback: (VerifyFlowWeb) -> Unit,
  private val setPromoCodeCallback: (promoCode: String) -> Unit,
  private val onLoginCallback: (authToken: String) -> Unit,
  private val goToUrlCallback: (url: String) -> Unit,
  private val updateCloseBehaviorCallback: (CloseBehaviorConfig) -> Unit,
) {

  @JavascriptInterface
  fun openIntercom() {
    intercomCallback()
  }

  @JavascriptInterface
  fun allowExternalApps(allow: Boolean) {
    allowExternalAppsCallback(allow)
  }

  @JavascriptInterface
  fun onPurchaseResult(result: String?) {
    onPurchaseResultCallback(parsePurchaseResult(result))
  }

  @JavascriptInterface
  fun openDeeplink(deepLink: String?): Boolean {
    onOpenDeepLink(deepLink)
    return deepLink != null
  }

  @JavascriptInterface
  fun startExternalPayment(deepLink: String?): Boolean {
    onStartExternalPayment(deepLink)
    return deepLink != null
  }

  @JavascriptInterface
  fun onError(result: String?) {
    onErrorCallback(parseError(result))
  }

  @JavascriptInterface
  fun openVerifyFlow(value: String?) {
    openVerifyFlowCallback(parseVerifyFlow(value))
  }

  @JavascriptInterface
  fun setPromoCode(promoCode: String) {
    setPromoCodeCallback(promoCode)
  }

  @JavascriptInterface
  fun onLogin(data: String) {
    Log.d("WebViewPaymentInterface", "onLogin payload: $data")
    val payload = parseLoginPayload(data)
    if (payload?.authToken.isNullOrBlank()) {
      logger.log("WebViewPaymentInterface", "onLogin: missing authToken in payload: $data")
      return
    } else {
      onLoginCallback(payload?.authToken ?: "")
    }
  }

  @JavascriptInterface
  fun goToUrl(url: String) {
    Log.d("WebViewPaymentInterface", "goToUrl: $url")
    goToUrlCallback(url)
  }

  @JavascriptInterface
  fun updateCloseBehavior(configJson: String) {
    Log.d("WebViewPaymentInterface", "updateCloseBehavior: $configJson")
    parseCloseBehaviorConfig(configJson)?.let {
      updateCloseBehaviorCallback(it)
    }
  }

  private fun parseCloseBehaviorConfig(json: String?): CloseBehaviorConfig? {
    return try {
      Gson().fromJson(json, CloseBehaviorConfig::class.java)
    } catch (e: Exception) {
      logger.log("WebViewPaymentInterface", "Failed to parse CloseBehaviorConfig: $json", e)
      null
    }
  }

  private fun parsePurchaseResult(result: String?): WebViewPaymentResponse? {
    Log.d("WebViewPaymentInterface", "Web Result before parse: $result")
    if (result.isNullOrBlank()) return null
    return try {
      val parsed = WebPaymentSuccessParser.WebResponse.fromJson(result)
      Log.d("WebViewPaymentInterface", "Web Result after parse: $parsed")
      WebViewPaymentResponse(
        responseCode = parsed.responseCode,
        purchaseData = parsed.purchaseData,
        dataSignature = parsed.dataSignature,
        orderReference = parsed.orderReference,
        paymentMethod = parsed.paymentMethod,
        isStoredCard = parsed.isStoredCard,
        wasCvcRequired = parsed.wasCvcRequired,
        uid = parsed.uid,
        hash = parsed.hash
      )
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  private fun parseError(result: String?): WebViewPaymentErrorResponse? {
    logger.log("WebCheckoutEvent", result, true, true)
    try {
      val responseModel = Gson().fromJson(result, WebViewPaymentErrorResponse::class.java)
      return responseModel
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }

  private fun parseVerifyFlow(result: String?): VerifyFlowWeb {
    return when (result) {
      VerifyFlowWeb.CREDIT_CARD.webValue -> VerifyFlowWeb.CREDIT_CARD
      VerifyFlowWeb.PAYPAL.webValue -> VerifyFlowWeb.PAYPAL
      else -> VerifyFlowWeb.CREDIT_CARD
    }
  }

  private data class LoginPayload(
    val authToken: String?,
  )

  private fun parseLoginPayload(json: String?): LoginPayload? {
    return try {
      Gson().fromJson(json, LoginPayload::class.java)
    } catch (e: Exception) {
      logger.log("WebViewPaymentInterface", "Failed to parse LoginPayload: $json", e)
      null
    }
  }

}
