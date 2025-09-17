package com.asfoundation.wallet.ui.webview_payment

import android.content.Intent
import android.net.Uri
import java.lang.ref.WeakReference

object PaymentOverlayHandle {
  @Volatile
  private var overlayRef: WeakReference<WebViewPaymentActivity>? = null

  fun register(activity: WebViewPaymentActivity) {
    overlayRef = WeakReference(activity)
  }

  fun unregister(activity: WebViewPaymentActivity) {
    overlayRef?.get()?.let { if (it === activity) overlayRef = null }
  }

  fun bringToFrontAndDeliver(uri: Uri?): Boolean {
    val overlay = overlayRef?.get() ?: return false
    overlay.runOnUiThread {
      val intent = Intent(overlay, WebViewPaymentActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .setData(uri)
      overlay.startActivity(intent)
    }
    return true
  }
}
