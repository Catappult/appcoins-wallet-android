package com.asfoundation.wallet.ui.webview_payment

import android.webkit.JavascriptInterface
import com.asfoundation.wallet.ui.webview_payment.models.VerifyFlowWeb
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentErrorResponse
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentResponse
import com.google.gson.Gson


class WebViewPaymentInterface(
  private val intercomCallback: () -> Unit,
  private val allowExternalAppsCallback: (allow: Boolean) -> Unit,
  private val onPurchaseResultCallback: (WebViewPaymentResponse?) -> Unit,
  private val onOpenDeepLink: (deepLink: String?) -> Unit,
  private val onErrorCallback: (WebViewPaymentErrorResponse?) -> Unit,
  private val openVerifyFlowCallback: (VerifyFlowWeb) -> Unit
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
    return deepLink!= null
  }

  @JavascriptInterface
  fun onError(result: String?) {
    onErrorCallback(parseError(result))
  }

  @JavascriptInterface
  fun openVerifyFlow(value: String?) {
    openVerifyFlowCallback(parseVerifyFlow(value))
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
