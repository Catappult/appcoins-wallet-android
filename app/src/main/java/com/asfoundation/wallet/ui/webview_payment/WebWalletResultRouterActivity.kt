package com.asfoundation.wallet.ui.webview_payment

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebWalletResultRouterActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val handled = PaymentOverlayHandle.bringToFrontAndDeliver(intent?.data)
    Log.d("WebWalletResultRouterActivity", "bringToFront: $handled")

    overridePendingTransition(0, 0)
    finish()
  }
}