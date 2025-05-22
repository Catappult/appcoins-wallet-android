package com.asfoundation.wallet.ui.webview_login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.autofill.AutofillManager
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.network.base.interceptors.UserAgentInterceptor
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.webview_payment.WebViewPaymentInterface
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@AndroidEntryPoint
class WebViewLoginActivity : AppCompatActivity() {


  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var analytics: BillingAnalytics

  @Inject
  lateinit var logger: Logger

  private val viewModel: WebViewLoginViewModel by viewModels()

  @Inject
  @ApplicationContext
  lateinit var context: Context

  @Inject
  lateinit var commonsPreferencesDataSource: CommonsPreferencesDataSource

  lateinit var userAgentInterceptor: UserAgentInterceptor

  private var webViewInstance: WebView? = null

  companion object {
    const val URL = "url"
    private val TAG = "WebView"
  }

  private val url: String by lazy {
    intent.getStringExtra(URL) ?: throw IllegalArgumentException("URL not provided")
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setResult(Activity.RESULT_CANCELED, Intent())
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay)
    setKeyboardListener()
    userAgentInterceptor = UserAgentInterceptor(context, commonsPreferencesDataSource)

    setContent {
      MainContent(url)
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    val data = intent?.data?.toString().orEmpty()

    viewModel.webView?.post {
      if (data.isNotBlank()) {
        viewModel.webView?.loadUrl("javascript:onPaymentStateUpdated(\"$data\")")
      } else {
        viewModel.webView?.loadUrl("javascript:onPaymentStateUpdated()")
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (viewModel.isFirstRun) {
      viewModel.isFirstRun = false
    } else {
      if (viewModel.runningCustomTab) {
        viewModel.webView?.loadUrl("javascript:onPaymentStateUpdated()")
        viewModel.runningCustomTab = false
      }
    }
  }

  var isPortraitSpaceForWeb = mutableStateOf(false)
  fun setKeyboardListener() {
    val decorView = window.decorView
    decorView.viewTreeObserver.addOnGlobalLayoutListener {
      val rect = Rect()
      decorView.getWindowVisibleDisplayFrame(rect)
      val ratio = (rect.height() * 0.8f) / rect.width()
      if (ratio > 1.05) {
        isPortraitSpaceForWeb.value = true
      } else {
        isPortraitSpaceForWeb.value = false
      }
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  @Composable
  fun MainContent(url: String) {
    Log.d("WebViewLogin", "starting login url: $url")
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val webView = remember {
      viewModel.webView ?: WebView(context).apply {
        setBackgroundColor(context.resources.getColor(R.color.transparent))
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.databaseEnabled = true
        settings.userAgentString =
          "Mozilla/5.0 (Linux; Android 14; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.78 Mobile Safari/537.36" //userAgentInterceptor.userAgent
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        CookieManager.getInstance().setAcceptCookie(true)

        webViewClient = object : WebViewClient() {
          override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url.isNullOrEmpty()) return false
            return false
          }
        }

        addJavascriptInterface(
          WebViewPaymentInterface(
            logger = logger,
            intercomCallback = {},
            allowExternalAppsCallback = {},
            onPurchaseResultCallback = {},
            onOpenDeepLink = {},
            onStartExternalPayment = {},
            onErrorCallback = {},
            openVerifyFlowCallback = {},
            setPromoCodeCallback = {},
            onLoginCallback = { authToken, safeLogin ->
              viewModel.fetchUserKey(authToken)
            },
            goToUrlCallback = { url ->
              loadUrl(url)
            },
          ),
          "WebViewPaymentInterface"
        )
        loadUrl(url)
        layoutParams = FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.MATCH_PARENT,
          FrameLayout.LayoutParams.MATCH_PARENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val autofillManager = context.getSystemService(AutofillManager::class.java)
          autofillManager?.notifyViewEntered(this)
        } else {
          settings.saveFormData = true
        }
        viewModel.webView = this
      }
    }

    BackHandler(enabled = true) {
      finish()
    }
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = if (isLandscape) 56.dp else 0.dp)
    ) {
      AndroidView(
        modifier = Modifier
          .fillMaxWidth()
          .background(styleguide_light_grey),
        factory = { webView }
      )
      when (val uiState = viewModel.uiState.collectAsState().value) {
        is WebViewLoginViewModel.UiState.FinishActivity -> {
          Log.d(TAG, "FinishActivity")
          finishActivity()
        }

        is WebViewLoginViewModel.UiState.FinishWithError -> {
          Log.d(TAG, "FinishWithError")
          finishWithError()
        }

        else -> {}
      }
    }
  }

  private fun finishActivity() {
    setResult(RESULT_OK)
    finish()
  }

  private fun finishWithError() {
    setResult(RESULT_CANCELED)
    finish()
  }

  override fun finish() {
    super.finish()
    overridePendingTransition(R.anim.stay, R.anim.slide_out_bottom)
  }

  private fun isDarkModeEnabled(context: Context): Boolean {
    return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
  }

}