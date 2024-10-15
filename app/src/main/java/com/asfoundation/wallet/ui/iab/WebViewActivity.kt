package com.asfoundation.wallet.ui.iab

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.AssetManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.appcoins.wallet.core.utils.android_common.Log
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewActivity : AppCompatActivity() {

  override fun getAssets(): AssetManager {
    //Workaround for crash when inflating the webView
    return if (Build.VERSION.SDK_INT > 22) {
      super.getAssets()
    } else {
      resources.assets
    }
  }

  private lateinit var billingWebViewFragment: BillingWebViewFragment

  @SuppressLint("SourceLockedOrientationActivity")
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.web_view_activity)
    if (savedInstanceState == null) {
      val forcePortrait = intent.getBooleanExtra(FORCE_PORTRAIT, false)
      if (forcePortrait && requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
      } else {
        lockCurrentPosition()
      }
      val url = intent.getStringExtra(URL)
      val htmlData = intent.getStringExtra(HTML_DATA)
      billingWebViewFragment = if (url.isNullOrEmpty())
        BillingWebViewFragment.newInstanceFromData(htmlData).apply { retainInstance = false }
      else
        BillingWebViewFragment.newInstance(url).apply { retainInstance = false }
      supportFragmentManager.beginTransaction()
        .add(R.id.container, billingWebViewFragment)
        .commit()
    }
  }

  override fun onBackPressed() {
    if (!((this::billingWebViewFragment.isInitialized) && billingWebViewFragment.handleBackPressed())) {
      super.onBackPressed()
    }
  }

  @SuppressLint("SourceLockedOrientationActivity")
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
    const val USER_CANCEL = 2
    private const val URL = "url"
    private const val HTML_DATA = "html_data"
    const val USER_CANCEL_THROWABLE = "user_cancel"
    const val FORCE_PORTRAIT = "${BuildConfig.APPLICATION_ID}.FORCE_PORTRAIT"

    fun newIntent(activity: Activity?, url: String?, forcePortrait: Boolean = false): Intent {
      return Intent(activity, WebViewActivity::class.java).apply {
        putExtra(URL, url)
        putExtra(HTML_DATA, "")
        putExtra(FORCE_PORTRAIT, forcePortrait)
      }
    }

    fun newIntentFromData(
      activity: Activity?,
      htmlData: String?,
      forcePortrait: Boolean = false
    ): Intent {
      return Intent(activity, WebViewActivity::class.java).apply {
        putExtra(URL, "")
        putExtra(HTML_DATA, htmlData)
        putExtra(FORCE_PORTRAIT, forcePortrait)
      }
    }
  }
}