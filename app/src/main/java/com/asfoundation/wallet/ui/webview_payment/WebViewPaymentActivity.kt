package com.asfoundation.wallet.ui.webview_payment

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.net.Uri.parse
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_webview_payment
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.PaymentMethodAnalyticsMapper
import com.asfoundation.wallet.backup.BackupNotificationUtils
import com.asfoundation.wallet.billing.paypal.usecases.CreateSuccessBundleUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.promotions.usecases.StartVipReferralPollingUseCase
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationService
import com.asfoundation.wallet.ui.iab.IabInteract
import com.asfoundation.wallet.ui.iab.IabInteract.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.webview_payment.models.VerifyFlowWeb
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity
import com.wallet.appcoins.feature.support.data.SupportInteractor
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
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
  lateinit var iabInteract: IabInteract

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var startVipReferralPollingUseCase: StartVipReferralPollingUseCase

  @Inject
  lateinit var analytics: BillingAnalytics

  @Inject
  lateinit var paymentAnalytics: PaymentMethodsAnalytics

  private val compositeDisposable = CompositeDisposable()

  private var shouldAllowExternalApps = true

  private var webViewInstance: WebView? = null

  companion object {
    private const val SUCCESS_SCHEMA = "https://wallet.dev.appcoins.io/iap/success"
    const val TRANSACTION_BUILDER = "transactionBuilder"
    const val URL = "url"
    private val TAG = "WebView"
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
    setKeyboardListener()
    setContent {
      MainContent(url)
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    val data = intent?.data?.toString().orEmpty()

    webViewInstance?.post {
      if (data.isNotBlank()) {
        webViewInstance?.loadUrl("javascript:onPaymentStateUpdated(\"$data\")")
      } else {
        webViewInstance?.loadUrl("javascript:onPaymentStateUpdated()")
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
    val webView = remember { WebView(context) }

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        factory = {
          WebView(context).apply {
            webViewInstance = this
            setBackgroundColor(resources.getColor(R.color.transparent))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            settings.databaseEnabled = true

            webViewClient = object : WebViewClient() {
              override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url.isNullOrEmpty()) return false
                return if (shouldAllowExternalApps) {
                  if (url.startsWith("http://") || url.startsWith("https://")) {
                    false
                  } else {
                    try {
                      val intent = Intent(Intent.ACTION_VIEW, parse(url))
                      view?.context?.startActivity(intent)
                      true
                    } catch (e: ActivityNotFoundException) {
                      true
                    }
                  }
                } else {
                  return false
                }
              }
            }

            addJavascriptInterface(
              WebViewPaymentInterface(
                intercomCallback = { showSupport() },
                allowExternalAppsCallback = {
                  shouldAllowExternalApps = it
                },
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
                    hash = webResult?.hash ?: "",
                    paymentMethod = webResult?.paymentMethod ?: ""
                  )
                },
                onErrorCallback = { webError ->
                  sendPaymentErrorEvent(
                    errorCode = webError?.errorCode ?: "",
                    errorReason = webError?.errorDetails ?: "",
                    paymentMethod = webError?.paymentMethod ?: ""
                  )
                },
                onOpenDeepLink = { deepLink: String? ->
                  val intent = Intent(Intent.ACTION_VIEW, parse(deepLink))
                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  startActivity(intent)
                },
                openVerifyFlowCallback = { verifyFlow ->
                  goToVerify(verifyFlow)
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
    hash: String,
    paymentMethod: String
  ) {
    compositeDisposable.add(
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
          sendPaymentEvent(paymentMethod)
          sendRevenueEvent()
          finish(it.bundle)
        }
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.io)
        .doOnError {
          Log.i(TAG, "createSuccessBundleAndFinish: ${it.message}")
          finish()
        }
        .subscribe({}, { Log.i(TAG, "createSuccessBundleAndFinish: ${it.message}") })
    )
  }

  override fun finish() {
    super.finish()
    overridePendingTransition(R.anim.stay, R.anim.slide_out_bottom)
  }

  fun showSupport() {
    compositeDisposable.add(
      supportInteractor.showSupport().subscribe({}, {})
    )
  }

  fun finish(bundle: Bundle) =
    if (bundle.getInt(AppcoinsBillingBinder.RESPONSE_CODE) == AppcoinsBillingBinder.RESULT_OK) {
      handleBackupNotifications(bundle)
      handlePerkNotifications(bundle)
    } else {
      Log.i(TAG, "finish: ${bundle.getInt(AppcoinsBillingBinder.RESPONSE_CODE)}")
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
          val mappedPaymentType = PaymentMethodAnalyticsMapper.mapPaymentToAnalytics(paymentMethod)
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
            isWebViewPayment = true,
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
          riskRules = null,
          isWebViewPayment = true,
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun isDarkModeEnabled(context: Context): Boolean {
    return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
  }

  fun handlePerkNotifications(bundle: Bundle) {
    compositeDisposable.add(iabInteract.getWalletAddress()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.io)
      .flatMap { startVipReferralPollingUseCase(Wallet(it)) }
      .doOnSuccess {
        PerkBonusAndGamificationService.buildService(this, it.address)
        finishActivity(bundle)
      }
      .doOnError { finishActivity(bundle) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun handleBackupNotifications(bundle: Bundle) {
    compositeDisposable.add(iabInteract.incrementAndValidateNotificationNeeded()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.io)
      .doOnSuccess { notificationNeeded ->
        if (notificationNeeded.isNeeded) {
          BackupNotificationUtils.showBackupNotification(
            context = this,
            walletAddress = notificationNeeded.walletAddress
          )
        }
        finishActivity(bundle)
      }
      .doOnError { finishActivity(bundle) }
      .subscribe({ }, { it.printStackTrace() })
    )
  }

  private fun sendPaymentEvent(paymentMethod: String) {
    compositeDisposable.add(Single.just(transactionBuilder)
      .subscribeOn(rxSchedulers.io)
      .subscribe { transactionBuilder ->
        stopTimingForPurchaseEvent(true, paymentMethod)
        analytics.sendPaymentEvent(
          transactionBuilder.domain,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          PaymentMethodAnalyticsMapper.mapPaymentToAnalytics(paymentMethod),
          transactionBuilder.type
        )
      })
  }

  private fun sendRevenueEvent() {
    compositeDisposable.add(Single.just(transactionBuilder)
      .doOnSuccess { transactionBuilder ->
        analytics.sendRevenueEvent(
          inAppPurchaseInteractor.convertToFiat(
            transactionBuilder.amount().toDouble(),
            BillingAnalytics.EVENT_REVENUE_CURRENCY
          )
            .subscribeOn(rxSchedulers.io)
            .blockingGet()
            .amount
            .setScale(2, BigDecimal.ROUND_UP)
            .toString()
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun stopTimingForPurchaseEvent(success: Boolean, paymentMethod: String) {
    val paymentMethodAnalytics = PaymentMethodAnalyticsMapper.mapPaymentToAnalytics(paymentMethod)
    paymentAnalytics.stopTimingForPurchaseEvent(paymentMethodAnalytics, success, false)
  }

  fun goToVerify(flow: VerifyFlowWeb) {
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

}