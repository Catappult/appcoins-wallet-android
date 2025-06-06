package com.asfoundation.wallet.ui.webview_payment

import android.util.Log
import android.webkit.JavascriptInterface
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.ui.webview_payment.models.VerifyFlowWeb
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentErrorResponse
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentResponse
import com.google.gson.Gson


class WebViewPaymentInterface(
  private val logger: Logger,
  private val intercomCallback: () -> Unit,
  private val allowExternalAppsCallback: (allow: Boolean) -> Unit,
  private val onPurchaseResultCallback: (WebViewPaymentResponse?) -> Unit,
  private val onOpenDeepLink: (deepLink: String?) -> Unit,
  private val onStartExternalPayment: (deepLink: String?) -> Unit,
  private val onErrorCallback: (WebViewPaymentErrorResponse?) -> Unit,
  private val openVerifyFlowCallback: (VerifyFlowWeb) -> Unit,
  private val setPromoCodeCallback: (promoCode: String) -> Unit,
  private val onLoginCallback: (authToken: String, safeLogin: Boolean) -> Unit,
  private val goToUrlCallback: (url: String) -> Unit,
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
  fun onLogin(authToken: String, safeLogin: Boolean) {
    Log.d("WebViewPaymentInterface", "onLogin: $authToken")
    onLoginCallback(authToken, safeLogin)
  }

  @JavascriptInterface
  fun goToUrl(url: String) {
    Log.d("WebViewPaymentInterface", "goToUrl: $url")
    goToUrlCallback(url)
  }


  private fun parsePurchaseResult(result: String?): WebViewPaymentResponse? {
    try {
      val responseModel = Gson().fromJson(result, WebViewPaymentResponse::class.java)
      return responseModel
    } catch (e: Exception) {
      e.printStackTrace()
      return null
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

}
