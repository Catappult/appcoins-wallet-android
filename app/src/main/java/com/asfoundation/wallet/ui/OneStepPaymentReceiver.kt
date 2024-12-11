package com.asfoundation.wallet.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.airbnb.lottie.LottieAnimationView
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.extensions.StringUtils.orEmpty
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletGetterStatus
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCountryCodeUseCase
import com.asf.wallet.R
import com.asfoundation.wallet.billing.paypal.PaypalReturnSchemas
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding.pending_payment.Quadruple
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.newIntent
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.ui.webview_payment.WebViewPaymentActivity
import com.asfoundation.wallet.util.TransferParser
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class OneStepPaymentReceiver : BaseActivity() {
  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var walletService: WalletService

  @Inject
  lateinit var ewtObtainer: EwtAuthenticatorService

  @Inject
  lateinit var getCountryCodeUseCase: GetCountryCodeUseCase

  @Inject
  lateinit var addressService: AddressService

  @Inject
  lateinit var rxSchedulers: RxSchedulers

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var transferParser: TransferParser

  @Inject
  lateinit var analytics: PaymentMethodsAnalytics

  @Inject
  lateinit var partnerAddressService: PartnerAddressService

  private var disposable: Disposable? = null
  private var walletCreationCard: View? = null
  private var walletCreationAnimation: LottieAnimationView? = null
  private var walletCreationText: View? = null

  private val resultAuthLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.data?.dataString?.contains(PaypalReturnSchemas.RETURN.schema) == true) {
        Log.d("WebViewPayment", "startWebViewAuthorization SUCCESS: ${result.data ?: ""}")

        //success

      } else if (
        result.resultCode == Activity.RESULT_CANCELED ||
        (result.data?.dataString?.contains(PaypalReturnSchemas.CANCEL.schema) == true)
      ) {
        Log.d("WebViewPayment", "startWebViewAuthorization CANCELED: ${result.data ?: ""}")
        // cancel
      }
    }

  companion object {
    const val REQUEST_CODE = 234
    private const val ESKILLS_URI_KEY = "ESKILLS_URI"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState == null) analytics.startTimingForOspTotalEvent()
    setContentView(R.layout.activity_iab_wallet_creation)
    walletCreationCard = findViewById(R.id.create_wallet_card)
    walletCreationAnimation = findViewById(R.id.create_wallet_animation)
    walletCreationText = findViewById(R.id.create_wallet_text)
    partnerAddressService.setOemIdFromSdk("")
    if (savedInstanceState == null) {
      disposable = handleWalletCreationIfNeeded()
        .takeUntil { it != WalletGetterStatus.CREATING.toString() }
        .filter { it != WalletGetterStatus.CREATING.toString() }
        .flatMap {
          if (isEskillsUri(intent.dataString!!)) {
            Toast.makeText(this, "Unavailable service.", Toast.LENGTH_SHORT).show()
            finish()
            Observable.just("")
          } else {
            transferParser.parse(intent.dataString!!)
              .flatMap { transaction: TransactionBuilder ->
                inAppPurchaseInteractor.isWalletFromBds(transaction.domain, transaction.toAddress())
                  .doOnSuccess { isBds: Boolean ->
                    // startOneStepTransfer(transaction, isBds) // TODO uncomment to use the old IAP
                  }
                  .flatMap { isBds: Boolean ->
                    startWebViewPayment(transaction)
                  }
              }.toObservable()
          }
        }
        .subscribe({ }, { throwable: Throwable ->
          logger.log("OneStepPaymentReceiver", throwable)
          startOneStepWithError(IabActivity.ERROR_RECEIVER_NETWORK)
        })
    }
  }

  @Suppress("DEPRECATION")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE) {
      setResult(resultCode, data)
      finish()
    }
  }

  private fun isEskillsUri(uri: String): Boolean = uri
    .lowercase(Locale.ROOT)
    .contains("/transaction/eskills")

  private fun startOneStepTransfer(
    transaction: TransactionBuilder,
    isBds: Boolean
  ) {
    val intent =
      newIntent(this, intent, transaction, isBds, transaction.payload)
    intent.putExtra(IabActivity.PRODUCT_NAME, transaction.skuId)
    @Suppress("DEPRECATION")
    startActivityForResult(intent, REQUEST_CODE)
  }

  private fun startWebViewPayment(
    transaction: TransactionBuilder,
  ): Single<String> {
    return buildWebViewPaymentUrl(transaction)
      .doOnSuccess { url ->
        launchWebViewPayment(url, transaction)
      }
  }

  val baseWebViewPaymentUrl = "https://wallet.dev.appcoins.io/iap"  //TODO from buildConfig

  private fun buildWebViewPaymentUrl(transaction: TransactionBuilder): Single<String> {
    return Single.zip(
      walletService.getAndSignCurrentWalletAddress().subscribeOn(rxSchedulers.io),
      ewtObtainer.getEwtAuthenticationNoBearer().subscribeOn(rxSchedulers.io), // TODO confirmar wallet usada
      getCountryCodeUseCase().subscribeOn(rxSchedulers.io),
      addressService.getAttribution(transaction?.domain ?: "").subscribeOn(rxSchedulers.io),
    ) { walletModel, ewt, country, oemId ->
      Quadruple(walletModel, ewt, country, oemId)
    }
      .map { args ->
        val walletModel = args.first
        val ewt = args.second
        val country = args.third
        val oemId = args.fourth.oemId

        "$baseWebViewPaymentUrl?" +
            "referrer_url=${URLEncoder.encode(transaction.referrerUrl, StandardCharsets.UTF_8.toString())}" +
            "&country=$country" +
            "&address=${walletModel.address}" +
            "&signature=${walletModel.signedAddress}" +
            "&payment_channel=wallet_app" +
            "&ewt=${ewt}" +
            "&origin=BDS" +
            "&product=${transaction.skuId}" +
            "&domain=${transaction.domain}" +
            "&type=${transaction.type}" +
            "&oem_id=${oemId ?: ""}" +
            "&reference=${transaction.orderReference ?: ""}"
      }
  }

  private fun startOneStepWithError(errorFromReceiver: String?) {
    val intent =
      newIntent(this, intent, errorFromReceiver)
    @Suppress("DEPRECATION")
    startActivityForResult(intent, REQUEST_CODE)
  }

  private fun launchWebViewPayment(url: String, transaction: TransactionBuilder) {
    val intentWebView = Intent(this, WebViewPaymentActivity::class.java).apply {
      putExtra(WebViewPaymentActivity.URL, url)
      putExtra(WebViewPaymentActivity.TRANSACTION_BUILDER, transaction)
    }
    @Suppress("DEPRECATION")
    startActivityForResult(intentWebView, REQUEST_CODE)
  }

  override fun onPause() {
    if (disposable != null && !disposable!!.isDisposed) {
      disposable!!.dispose()
    }
    super.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()
    walletCreationCard = null
    walletCreationAnimation = null
    walletCreationText = null
  }

  private fun handleWalletCreationIfNeeded(): Observable<String> =
    walletService.findWalletOrCreate()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        if (it == WalletGetterStatus.CREATING.toString()) {
          showLoadingAnimation()
        }
      }
      .filter { it != WalletGetterStatus.CREATING.toString() }
      .map {
        endAnimation()
        it
      }

  private fun endAnimation() {
    walletCreationAnimation!!.visibility = View.INVISIBLE
    walletCreationCard!!.visibility = View.INVISIBLE
    walletCreationText!!.visibility = View.INVISIBLE
    walletCreationAnimation!!.removeAllAnimatorListeners()
    walletCreationAnimation!!.removeAllUpdateListeners()
    walletCreationAnimation!!.removeAllLottieOnCompositionLoadedListener()
  }

  private fun showLoadingAnimation() {
    walletCreationAnimation!!.visibility = View.VISIBLE
    walletCreationCard!!.visibility = View.VISIBLE
    walletCreationText!!.visibility = View.VISIBLE
    walletCreationAnimation!!.playAnimation()
  }
}