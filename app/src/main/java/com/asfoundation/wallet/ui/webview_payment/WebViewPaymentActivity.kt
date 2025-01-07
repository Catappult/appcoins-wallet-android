package com.asfoundation.wallet.ui.webview_payment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_webview_payment
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Single
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

  @Inject
  lateinit var analytics: BillingAnalytics

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
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay)
    setContent {
      MainContent(url)
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  @Composable
  fun MainContent(url: String) {
    Log.i("WebView", "starting url: $url")
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val webView = remember { WebView(context) }

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val topSpacerHeight = if (isLandscape) 36.dp else 176.dp

    BackHandler(enabled = true) {
      if (webView.canGoBack()) {
        webView.goBack()
      } else {
        finish()
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = if (isLandscape) 56.dp else 0.dp)
    ) {
      Spacer(
        modifier = Modifier
          .height(topSpacerHeight)
          .fillMaxWidth()
          .clickable {
            finish()
          }
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(17.dp)
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
                    finishActivity(Bundle().apply {
                      putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
                    })
                    return true
                  }
                }
                return false
              }
            }

            addJavascriptInterface(
              WebViewPaymentInterface(
                context = context,
                intercomCallback = { showSupport(0) },
                onPurchaseResultCallback = { webResult ->
                  sendPaymentSuccessEvent(
                    webResult?.uid ?: "",
                    webResult?.paymentMethod ?: "",
                    webResult?.isStoredCard ?: false,
                    webResult?.wasCvcRequired ?: false
                  )
                  createSuccessBundleAndFinish(
                    type = transactionBuilder.type,
                    merchantName = transactionBuilder.domain,
                    sku = transactionBuilder.skuId,
                    purchaseUid = webResult?.uid ?: "",
                    orderReference = webResult?.orderReference ?: "",
                    hash = webResult?.hash ?: ""
                  )
                },
                onErrorCallback = { webError ->
                  sendPaymentErrorEvent(
                    errorCode = webError?.errorCode ?: "",
                    errorReason = webError?.errorDetails ?: "",
                    paymentMethod = webError?.paymentMethod ?: ""
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

  private fun createSuccessBundleAndFinish(
    type: String,
    merchantName: String,
    sku: String,
    purchaseUid: String,
    orderReference: String,
    hash: String
  ) {
    createSuccessBundleUseCase(
      type = type,
      merchantName = merchantName,
      sku = sku,
      purchaseUid = purchaseUid,
      orderReference = orderReference,
      hash = hash,
      scheduler = rxSchedulers.io
    )
      .doOnSuccess {
//        sendPaymentEvent(transactionBuilder)
//        sendRevenueEvent(transactionBuilder)
        finish(it.bundle)
      }
      .subscribeOn(rxSchedulers.main)
      .observeOn(rxSchedulers.main)
      .doOnError {
        // TODO handle error log
        finish()
      }
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
    data.remove(PRE_SELECTED_PAYMENT_METHOD_KEY)
    setResult(RESULT_OK, Intent().putExtras(data))
    finish()
  }

  private fun sendPaymentSuccessEvent(
    uid: String,
    paymentMethod: String,
    isStoredCard: Boolean,
    wasCvcRequired: Boolean
  ) {
    compositeDisposable.add(
      Single.just(transactionBuilder)
      .observeOn(rxSchedulers.io)
      .doOnSuccess { transaction ->
        val mappedPaymentType = mapPaymentToAnalytics(paymentMethod)
        analytics.sendPaymentSuccessEvent(
          packageName = transactionBuilder.domain,
          skuDetails = transaction.skuId,
          value = transaction.amount().toString(),
          purchaseDetails = paymentMethod,
          transactionType = transaction.type,
          txId = uid,
          valueUsd = transaction.amountUsd.toString(),
          isStoredCard =
          if (mappedPaymentType == PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
            isStoredCard
          else null,
          wasCvcRequired =
          if (mappedPaymentType == PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
            wasCvcRequired
          else null,
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun sendPaymentErrorEvent(
    errorCode: String,
    errorReason: String,
    paymentMethod: String
  ) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .observeOn(rxSchedulers.io)
      .doOnSuccess { transaction ->
        analytics.sendPaymentErrorWithDetailsAndRiskEvent(
          packageName = transaction.domain,
          skuDetails = transaction.skuId,
          value = transaction.amount().toString(),
          purchaseDetails = paymentMethod,
          transactionType = transaction.type,
          errorCode = errorCode,
          errorDetails = errorReason,
          riskRules = null
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun mapPaymentToAnalytics(paymentType: String): String =
    if (paymentType == PaymentType.CARD.name) {
      PaymentMethodsAnalytics.PAYMENT_METHOD_CC
    } else {
      PaymentMethodsAnalytics.PAYMENT_METHOD_PP
    }

  fun isDarkModeEnabled(context: Context): Boolean {
    return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
  }

}