package com.asfoundation.wallet.ui.webview_payment

import android.net.Uri
import android.webkit.WebView
import org.json.JSONObject

/**
 * Calls the page's `onPaymentStateUpdated` with [data] as a quoted JS string, so a crafted
 * result link cannot inject script. The previous `loadUrl("javascript:…")` form percent-decoded
 * the argument, so it is decoded here to keep what the page receives unchanged.
 */
fun WebView.notifyPaymentStateUpdated(data: String) {
  val arg = if (data.isBlank()) "" else JSONObject.quote(Uri.decode(data))
  evaluateJavascript("onPaymentStateUpdated($arg)", null)
}
