package com.asfoundation.wallet.ui.webview_payment

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast


class WebViewPaymentInterface(
  private val context: Context,
  private val intercomCallback: () -> Unit
  ) {

  @JavascriptInterface
  fun openIntercom() {
    intercomCallback()
  }
}