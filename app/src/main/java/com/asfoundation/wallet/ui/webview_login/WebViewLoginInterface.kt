package com.asfoundation.wallet.ui.webview_login

import android.util.Log
import android.webkit.JavascriptInterface
import com.asfoundation.wallet.ui.webview_payment.models.VerifyFlowWeb
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentErrorResponse
import com.asfoundation.wallet.ui.webview_payment.models.WebViewPaymentResponse
import com.google.gson.Gson
import com.appcoins.wallet.core.utils.jvm_common.Logger


class WebViewLoginInterface(
  private val logger: Logger,
  private val onPurchaseResultCallback: (WebViewPaymentResponse?) -> Unit,
) {

//  @JavascriptInterface
//  fun onPurchaseResult(result: String?) {
//    onPurchaseResultCallback(parsePurchaseResult(result))
//  }

}
