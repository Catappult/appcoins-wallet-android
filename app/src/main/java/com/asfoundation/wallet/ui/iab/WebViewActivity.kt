package com.asfoundation.wallet.ui.iab

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.asf.wallet.R
import dagger.android.AndroidInjection

class WebViewActivity : AppCompatActivity() {

  public override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.web_view_activity)
    lockCurrentPosition()

    if (savedInstanceState == null) {
      val url = intent.getStringExtra(URL)
      val billingWebViewFragment = BillingWebViewFragment.newInstance(url)
      supportFragmentManager.beginTransaction()
          .add(R.id.container, billingWebViewFragment)
          .commit()
    }
  }

  private fun lockCurrentPosition() {
    //setRequestedOrientation requires translucent and floating to be false to work in API 26
    val rotation = windowManager.defaultDisplay
        .rotation
    val orientation = resources.configuration.orientation

    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
      if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
      }
    } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
      if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
      } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
      }
    } else {
      Log.w("WebView", "Invalid orientation value: $orientation")
    }

  }

  companion object {

    const val SUCCESS = 1
    const val FAIL = 0
    private const val URL = "url"

    fun newIntent(activity: Activity?, url: String?): Intent {
      return Intent(activity, WebViewActivity::class.java).apply {
        putExtra(URL, url)
      }
    }
  }
}