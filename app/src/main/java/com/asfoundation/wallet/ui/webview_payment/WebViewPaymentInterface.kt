package com.asfoundation.wallet.ui.webview_payment

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentErrorResponse
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentResponse
import com.google.gson.Gson


class WebViewPaymentInterface(
  private val intercomCallback: () -> Unit,
  private val onPurchaseResultCallback: (WebViewPaymentResponse?) -> Unit,
  private val onErrorCallback: (WebViewPaymentErrorResponse?) -> Unit
  ) {

  @JavascriptInterface
  fun openIntercom() {
    intercomCallback()
  }

  @JavascriptInterface
  fun onPurchaseResult(result: String?) {
    onPurchaseResultCallback(parsePurchaseResult(result))
  }

  @JavascriptInterface
  fun onError(result: String?) {
    onErrorCallback(parseError(result))
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

}
