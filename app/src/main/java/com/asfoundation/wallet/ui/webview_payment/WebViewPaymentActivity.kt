package com.asfoundation.wallet.ui.webview_payment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_webview_payment
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.asf.wallet.R
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class WebViewPaymentActivity : AppCompatActivity() {

  @Inject
  lateinit var createSuccessBundleUseCase: CreateSuccessBundleUseCase

  @Inject
  lateinit var rxSchedulers: RxSchedulers

  @Inject
  lateinit var supportInteractor: SupportInteractor

  private val compositeDisposable = CompositeDisposable()

  companion object {
    private const val SUCCESS_SCHEMA = "https://wallet.dev.appcoins.io/iap/success"
    const val TRANSACTION_BUILDER = "transactionBuilder"
    const val URL = "url"
  }

  private val url: String by lazy {
    intent.getStringExtra(URL) ?: throw IllegalArgumentException("URL not provided")
  }

  private val transactionBuilder: TransactionBuilder by lazy<TransactionBuilder> {
    intent.getParcelableExtra(TRANSACTION_BUILDER)
      ?: throw IllegalArgumentException("TransactionBuilder not provided")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay);
    setContent {
      MainContent(url)
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  @Composable
  fun MainContent(url: String) {
    Log.i("WebView", "starting url: $url")
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    BackHandler(enabled = true) {
      if (webView?.canGoBack() == true) {
        webView?.goBack()
      } else {
        finish()
      }
    }

    Column(modifier = Modifier.fillMaxSize()) {
      Spacer(
        modifier = Modifier
          .height(200.dp)
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .background(
            color = if (isDarkModeEnabled(context))
              styleguide_blue_webview_payment
            else
              styleguide_light_grey,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
          )
      )
      AndroidView(
        modifier = Modifier
          .fillMaxWidth()
          .background(styleguide_light_grey),
        factory = {
          webView.apply {
            setBackgroundColor(resources.getColor(R.color.transparent))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            webViewClient = object : WebViewClient() {
              override fun shouldOverrideUrlLoading(view: WebView, clickUrl: String): Boolean {
                when {
                  clickUrl.contains(SUCCESS_SCHEMA) -> {
//                    createSuccessBundleAndFinish() // TODO activate
                    finishActivity(Bundle().apply {
                      putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
                    })
                    return true
                  }

                }
                return false
              }

//              override fun onPageFinished(view: WebView, url: String) {
//                super.onPageFinished(view, url)
//                if (!url.contains("/redirect")) {
//                  val timeout = timeoutReference.getAndSet(null)
//                  timeout?.cancel(false)
//                  try {
//                    binding.webviewProgressBar.visibility = View.GONE
//                  } catch (exception: Exception) {
//                    logger.log(TAG, exception)
//                  }
//                }
//                if (url.contains(ASYNC_PAYMENT_FORM_SHOWN_SCHEMA)) {
//                  asyncDetailsShown = true
//                }
//              }
            }

            addJavascriptInterface(
              WebViewPaymentInterface(
                context = context,
                intercomCallback = { showSupport(0) },
                onPurchaseResultCallback = { webResult ->
                  createSuccessBundleAndFinish(
                    type = transactionBuilder.type, //"INAPP_UNMANAGED",
                    merchantName = transactionBuilder.domain, //"test",
                    sku = transactionBuilder.skuId, //"oilTest",
                    purchaseUid = webResult?.uid ?: "",
                    orderReference = webResult?.orderReference ?: "",
                    hash = webResult?.hash ?: ""
                  )
                }
              ),
              "WebViewPaymentInterface"
            )

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
              settings.saveFormData = true
            } else {
              val autofillManager = getSystemService(AutofillManager::class.java)
              autofillManager?.notifyViewEntered(this)
            }
            CookieManager.getInstance()
              .setAcceptCookie(true)
            loadUrl(url)
            layoutParams = FrameLayout.LayoutParams(
              FrameLayout.LayoutParams.MATCH_PARENT,
              FrameLayout.LayoutParams.MATCH_PARENT
            )
          }
        }
      )

    }
  }

  fun createSuccessBundleAndFinish(
    type: String,
    merchantName: String,
    sku: String,
    purchaseUid: String,
    orderReference: String,
    hash: String
  ) { // TODO obtain from web success page
    createSuccessBundleUseCase(
      type = type, // "INAPP_UNMANAGED", //transactionBuilder.type,
      merchantName = merchantName, //"test", //transactionBuilder.domain,
      sku = sku, // "oilTest", //transactionBuilder.skuId,
      purchaseUid = purchaseUid, // "1234", //purchaseUid,
      orderReference = orderReference, // "1234", //orderReference,
      hash = hash, //"1234", //hash,
      scheduler = rxSchedulers.io
    )
      .doOnSuccess {
//        sendPaymentEvent(transactionBuilder)
//        sendRevenueEvent(transactionBuilder)
        finish(it.bundle)
      }
      .subscribeOn(rxSchedulers.main)
      .observeOn(rxSchedulers.main)
//      .doOnError {
//        finish(it.) // TODO handle error
//      }
      .subscribe()
  }

  override fun finish() {
    super.finish()
    overridePendingTransition(R.anim.stay, R.anim.slide_out_bottom)
  }

  fun showSupport(gamificationLevel: Int) {
    compositeDisposable.add(
      supportInteractor.showSupport(gamificationLevel).subscribe({}, {})
    )
  }

  fun finish(bundle: Bundle) =
    if (bundle.getInt(AppcoinsBillingBinder.RESPONSE_CODE) == AppcoinsBillingBinder.RESULT_OK) {
//      handleBackupNotifications(bundle)
//      handlePerkNotifications(bundle)
      finishActivity(bundle)
    } else {
      finishActivity(bundle)
    }

  fun finishActivity(data: Bundle) {
//    savePreselectedPaymentMethod(data)
//    data.remove(PRE_SELECTED_PAYMENT_METHOD_KEY)
    setResult(Activity.RESULT_OK, Intent().putExtras(data))
    finish()
  }

  fun isDarkModeEnabled(context: Context): Boolean {
    return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
  }

}