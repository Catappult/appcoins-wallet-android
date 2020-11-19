package com.asfoundation.wallet.ui.iab

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.webview_fragment.*
import kotlinx.android.synthetic.main.webview_fragment.view.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class BillingWebViewFragment : BasePageViewFragment() {
  private val timeoutReference: AtomicReference<ScheduledFuture<*>?> = AtomicReference()

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var analytics: BillingAnalytics
  private var currentUrl: String? = null
  private var executorService: ScheduledExecutorService? = null
  private var webViewActivity: WebViewActivity? = null
  private var asyncDetailsShown = false

  companion object {
    private const val ADYEN_PAYMENT_SCHEMA = "adyencheckout://"
    private const val LOCAL_PAYMENTS_SCHEMA = "myappcoins.com/t/"
    private const val LOCAL_PAYMENTS_URL = "https://myappcoins.com/t/"
    private const val GO_PAY_APP_PAYMENTS_SCHEMA = "gojek://"
    private const val LINE_APP_PAYMENTS_SCHEMA = "intent://"
    private const val ASYNC_PAYMENT_FORM_SHOWN_SCHEMA = "https://pm.dlocal.com//v1/gateway/show?"
    private const val CODAPAY_FINAL_REDIRECT_SCHEMA =
        "https://airtime.codapayments.com/epcgw/dlocal/"
    private const val CODAPAY_BACK_URL =
        "https://pay.dlocal.com/payment_method_connectors/global_pm//back"
    private const val CODAPAY_CANCEL_URL =
        "codapayments.com/airtime/cancelConfirm"
    private const val URL = "url"
    private const val CURRENT_URL = "currentUrl"
    private const val ORDER_ID_PARAMETER = "OrderId"
    const val OPEN_SUPPORT = BuildConfig.MY_APPCOINS_BASE_HOST + "open-support"

    fun newInstance(url: String?): BillingWebViewFragment {
      return BillingWebViewFragment().apply {
        arguments = Bundle().apply {
          putString(URL, url)
        }
        retainInstance = true
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(context is WebViewActivity) { "WebView fragment must be attached to WebView Activity" }
    webViewActivity = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    executorService = Executors.newScheduledThreadPool(0)
    require(arguments != null && arguments!!.containsKey(URL)) { "Provided url is null!" }
    currentUrl = if (savedInstanceState == null) {
      arguments!!.getString(URL)
    } else {
      savedInstanceState.getString(CURRENT_URL)
    }
    CookieManager.getInstance()
        .setAcceptCookie(true)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.webview_fragment, container, false)

    view.webview.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView, clickUrl: String): Boolean {
        when {
          clickUrl.contains(LOCAL_PAYMENTS_SCHEMA) || clickUrl.contains(ADYEN_PAYMENT_SCHEMA) -> {
            currentUrl = clickUrl
            finishWithSuccess(clickUrl)
          }
          clickUrl.contains(GO_PAY_APP_PAYMENTS_SCHEMA) || clickUrl.contains(
              LINE_APP_PAYMENTS_SCHEMA) -> {
            launchActivity(Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl)))
          }
          clickUrl.contains(CODAPAY_FINAL_REDIRECT_SCHEMA) && clickUrl.contains(
              ORDER_ID_PARAMETER) -> {
            val orderId = Uri.parse(clickUrl)
                .getQueryParameter(ORDER_ID_PARAMETER)
            finishWithSuccess(LOCAL_PAYMENTS_URL + orderId)
          }
          clickUrl.contains(CODAPAY_CANCEL_URL) -> finishWithFail(clickUrl)
          clickUrl.contains(OPEN_SUPPORT) -> finishWithFail(clickUrl)
          else -> {
            currentUrl = clickUrl
            return false
          }
        }
        return true
      }

      override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (!url.contains("/redirect")) {
          val timeout = timeoutReference.getAndSet(null)
          timeout?.cancel(false)
          webview_progress_bar?.visibility = View.GONE
        }
        if (url.contains(ASYNC_PAYMENT_FORM_SHOWN_SCHEMA)) {
          asyncDetailsShown = true
        }
      }
    }
    view.webview.settings.javaScriptEnabled = true
    view.webview.settings.domStorageEnabled = true
    view.webview.settings.useWideViewPort = true
    view.webview.loadUrl(currentUrl)
    return view
  }

  fun handleBackPressed(): Boolean {
    return if (asyncDetailsShown) {
      webview.loadUrl(CODAPAY_BACK_URL)
      asyncDetailsShown = false
      true
    } else {
      false
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(CURRENT_URL, currentUrl)
  }

  override fun onDestroy() {
    executorService!!.shutdown()
    super.onDestroy()
  }

  override fun onDetach() {
    webViewActivity = null
    webview?.webViewClient = null
    super.onDetach()
  }

  private fun launchActivity(intent: Intent) {
    try {
      startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      if (view != null) {
        Snackbar.make(view!!, R.string.unknown_error,
            Snackbar.LENGTH_SHORT)
            .show()
      }
    }
  }

  private fun finishWithSuccess(url: String) {
    val intent = Intent()
    intent.data = Uri.parse(url)
    webViewActivity!!.setResult(WebViewActivity.SUCCESS, intent)
    webViewActivity!!.finish()
  }

  private fun finishWithFail(url: String) {
    val intent = Intent()
    intent.data = Uri.parse(url)
    webViewActivity!!.setResult(WebViewActivity.FAIL, intent)
    webViewActivity!!.finish()
  }
}