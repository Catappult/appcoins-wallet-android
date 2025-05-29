package com.asfoundation.wallet.ui.webview_payment

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri.parse
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.autofill.AutofillManager
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.network.base.interceptors.UserAgentInterceptor
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_webview_payment
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.asf.wallet.R
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.ui.OneStepPaymentReceiver
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.webview_payment.models.VerifyFlowWeb
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class WebViewPaymentActivity : AppCompatActivity() {


  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var analytics: BillingAnalytics

  @Inject
  lateinit var logger: Logger

  private val viewModel: WebViewPaymentViewModel by viewModels()

  @Inject
  @ApplicationContext
  lateinit var context: Context

  @Inject
  lateinit var commonsPreferencesDataSource: CommonsPreferencesDataSource

  lateinit var userAgentInterceptor: UserAgentInterceptor

  private val compositeDisposable = CompositeDisposable()

  private var shouldAllowExternalApps = true

  private var webViewInstance: WebView? = null

  companion object {
    private const val SUCCESS_SCHEMA = "https://wallet.dev.appcoins.io/iap/success"
    const val TRANSACTION_BUILDER = "transactionBuilder"
    const val URL = "url"
    const val TYPE = "type"
    const val SDK_TRANSACTION = "sdkTransaction"
    const val OSP_TRANSACTION = "ospTransaction"
    private val TAG = "WebView"
    val LOGIN_URLS = arrayOf(
      "iap/sign-in",
      "wallet/sign-in",
      "accounts.google.com",
      "/api/auth/google/callback"
    )
  }

  private val url: String by lazy {
    intent.getStringExtra(URL) ?: throw IllegalArgumentException("URL not provided")
  }

  private val transactionBuilder: TransactionBuilder by lazy<TransactionBuilder> {
    intent.getParcelableExtra(TRANSACTION_BUILDER)
      ?: throw IllegalArgumentException("TransactionBuilder not provided")
  }

  private val type: String by lazy {
    intent.getStringExtra(TYPE) ?: throw IllegalArgumentException("Type not provided")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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
    Log.d("WebView", "starting url: $url")
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val webView = remember {
      viewModel.webView ?: WebView(context).apply {
        setBackgroundColor(context.resources.getColor(R.color.transparent))
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.databaseEnabled = true
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        CookieManager.getInstance().setAcceptCookie(true)

        webViewClient = object : WebViewClient() {
          override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url.isNullOrEmpty()) return false

            if (LOGIN_URLS.any { url.contains(it, ignoreCase = true) }) {
              Log.d(TAG, "Login URL detected: $url")
              val newUa = buildUA()
              Log.d(TAG, "Setting user agent Google")
              if (settings.userAgentString != newUa) {
                settings.userAgentString = newUa
                loadUrl(url)
                return true
              }
            } else {
              val defaultUa = WebSettings.getDefaultUserAgent(context)
              if (settings.userAgentString != defaultUa) {
                Log.d(TAG, "Setting user agent Default")
                settings.userAgentString = defaultUa
                loadUrl(url)
                return true
              }
            }

            return if (shouldAllowExternalApps) {
              if (url.startsWith("http://") || url.startsWith("https://")) {
                false
              } else {
                try {
                  val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                  context.startActivity(intent)
                  true
                } catch (e: ActivityNotFoundException) {
                  true
                }
              }
            } else {
              false
            }
          }
        }

        addJavascriptInterface(
          WebViewPaymentInterface(
            logger = logger,
            intercomCallback = { viewModel.showSupport() },
            allowExternalAppsCallback = {
              shouldAllowExternalApps = it
            },
            onPurchaseResultCallback = { webResult ->
              viewModel.sendPaymentSuccessEvent(
                webResult?.uid ?: "",
                webResult?.paymentMethod ?: "",
                webResult?.isStoredCard ?: false,
                webResult?.wasCvcRequired ?: false,
                transactionBuilder = transactionBuilder
              )
              viewModel.createSuccessBundleAndFinish(
                type = transactionBuilder.type,
                merchantName = transactionBuilder.domain,
                sku = transactionBuilder.skuId,
                purchaseUid = webResult?.uid ?: "",
                orderReference = webResult?.orderReference ?: "",
                hash = webResult?.hash ?: "",
                paymentMethod = webResult?.paymentMethod ?: "",
                transactionBuilder = transactionBuilder
              )
            },
            onErrorCallback = { webError ->
              viewModel.sendPaymentErrorEvent(
                errorCode = webError?.errorCode ?: "",
                errorReason = webError?.errorDetails ?: "",
                paymentMethod = webError?.paymentMethod ?: "",
                transactionBuilder = transactionBuilder
              )
            },
            onStartExternalPayment = { deepLink: String? ->
              val intent = CustomTabsIntent.Builder().build()
              intent.launchUrl(getContext(), parse(deepLink))
              viewModel.runningCustomTab = true
            },
            onOpenDeepLink = { deepLink: String? ->
              val intent = Intent(Intent.ACTION_VIEW, parse(deepLink))
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              startActivity(intent)
            },
            openVerifyFlowCallback = { verifyFlow ->
              goToVerify(verifyFlow)
            },
            setPromoCodeCallback = { promoCode ->
              viewModel.setPromoCode(promoCode)
            },
            onLoginCallback = { authToken, safeLogin ->
              Log.d(TAG, "onLoginCallback called")
              viewModel.fetchUserKey(authToken, type, transactionBuilder)
            },
            goToUrlCallback = { },
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
      if (isLandscape) {
        Spacer(
          modifier = Modifier
            .height(36.dp)
            .fillMaxWidth()
            .clickable { finish() }
        )
      } else {
        Spacer(
          modifier = Modifier
            .fillMaxWidth()
            .weight(if (isPortraitSpaceForWeb.value) 0.2f else 0.02f)
            .clickable { finish() }
        )
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(16.dp)
          .background(
            color = if (isDarkModeEnabled(context))
              styleguide_blue_webview_payment
            else
              styleguide_light_grey,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
          )
      )
      AndroidView(
        modifier = Modifier
          .fillMaxWidth()
          .weight(
            if (isLandscape)
              1f
            else
              if (isPortraitSpaceForWeb.value)
                0.8f
              else
                0.98f
          )
          .background(styleguide_light_grey),
        factory = { webView }
      )
      when (val uiState = viewModel.uiState.collectAsState().value) {
        is WebViewPaymentViewModel.UiState.FinishActivity -> finishActivity(uiState.bundle)
        WebViewPaymentViewModel.UiState.Finish -> finish()
        is WebViewPaymentViewModel.UiState.FinishWithBundle -> {
          viewModel.sendRevenueEvent(transactionBuilder)
          finish(uiState.bundle)
        }
        is WebViewPaymentViewModel.UiState.LoadUrl -> {
          webView.loadUrl(uiState.url)
        }

        else -> {}
      }
    }
  }


  override fun finish() {
    super.finish()
    overridePendingTransition(R.anim.stay, R.anim.slide_out_bottom)
  }

  fun finish(bundle: Bundle) =
    if (bundle.getInt(AppcoinsBillingBinder.RESPONSE_CODE) == AppcoinsBillingBinder.RESULT_OK) {
      viewModel.handleBackupNotifications(bundle, context = this)
      viewModel.handlePerkNotifications(bundle, context = this)
    } else {
      Log.i(TAG, "finish bundle: ${bundle.getInt(AppcoinsBillingBinder.RESPONSE_CODE)}")
      finishActivity(bundle)
    }

  private fun finishActivity(data: Bundle) {
    data.remove(PRE_SELECTED_PAYMENT_METHOD_KEY)
    setResult(RESULT_OK, Intent().putExtras(data))
    finish()
  }


  private fun isDarkModeEnabled(context: Context): Boolean {
    return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
  }


  private fun goToVerify(flow: VerifyFlowWeb) {
    when (flow) {
      VerifyFlowWeb.CREDIT_CARD -> {
        showCreditCardVerification(false)
      }

      VerifyFlowWeb.PAYPAL -> {
        showPayPalVerification()
      }
    }
  }

  fun showCreditCardVerification(isWalletVerified: Boolean) {
    val intent = VerificationCreditCardActivity.newIntent(this, isWalletVerified)
      .apply { intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
    startActivity(intent)
    finish()
  }

  fun showPayPalVerification() {
    val intent = MainActivity.newIntent(
      context = this,
      supportNotificationClicked = false,
      isPayPalVerificationRequired = true
    ).apply { intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
    startActivity(intent)
    finish()
  }

  private fun buildUA(): String {
    return WebSettings.getDefaultUserAgent(context)
      .replace("; wv", "")
      .replace(Regex("""\s*Version/\d+\.\d+\s*"""), "")
      .trim()
  }

}