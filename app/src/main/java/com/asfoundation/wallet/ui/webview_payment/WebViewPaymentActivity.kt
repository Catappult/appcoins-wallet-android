package com.asfoundation.wallet.ui.webview_payment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.autofill.AutofillManager
import android.webkit.CookieManager
import android.webkit.WebSettings
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.asf.wallet.R
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
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
  }

  private val url: String by lazy {
    intent.getStringExtra("url") ?: throw IllegalArgumentException("URL not provided")
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
      Spacer(modifier = Modifier
        .height(200.dp)
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(16.dp)
          .background(
            color = styleguide_light_grey,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
          )
      )
      AndroidView(
        modifier = Modifier
          .fillMaxWidth()
          .background(styleguide_light_grey),
        factory = {
          webView.apply {
            setBackgroundColor(resources.getColor(R. color. transparent))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            WebSettingsCompat.setForceDark(
              settings,
              WebSettingsCompat.FORCE_DARK_ON
            )
            webViewClient = object : WebViewClient() {
              override fun shouldOverrideUrlLoading(view: WebView, clickUrl: String): Boolean {
                when {
                  clickUrl.contains(SUCCESS_SCHEMA) -> {
//                    createSuccessBundleAndFinish(). // TODO activate
                    finishActivity( Bundle().apply {
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
                context
              ) { showSupport(0) },
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

  fun createSuccessBundleAndFinish() { // TODO obtain from web success page
    createSuccessBundleUseCase(
      type = "INAPP_UNMANAGED", //transactionBuilder.type,
      merchantName = "test", //transactionBuilder.domain,
      sku = "oilTest", //transactionBuilder.skuId,
      purchaseUid = "1234", //purchaseUid,
      orderReference = "1234", //orderReference,
      hash = "1234", //hash,
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

}