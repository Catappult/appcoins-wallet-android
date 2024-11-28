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
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletGetterStatus
import com.asf.wallet.R
import com.asfoundation.wallet.billing.paypal.PaypalReturnSchemas
import com.asfoundation.wallet.entity.TransactionBuilder
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
        launchWebViewPayment(url)
      }

//    val intentWebView = WebViewActivity.newIntent(this, url)
//    resultAuthLauncher.launch(intentWebView)
  }

  //TODO remove, example of WebView Payment url
  val test_url_osp = "https://wallet.dev.appcoins.io/iap?referrer_url=https%3A%2F%2Fapichain.dev.catappult.io%2Ftransaction%2Finapp%3Fproduct%3Doil%26value%3D0.05%26currency%3DUSD%26callback_url%3Dhttps%253A%252F%252Fapi.dev.catappult.io%252Fbroker%252F8.20200101%252Fmock%252Fcallback%26domain%3Dcom.appcoins.trivialdrivesample.test%26signature%3D570c49c27cb916c595744e73d0aca61faf8ebae16603d90504ed8677ac5d4504&country=PT&address=0x9b80a0d785ee8046bc1a0545b80627fe86a32eba&signature=9ac61e132c432ce8cd86bdf5f8f84a2bbbd97ab6c09341cc63ec7a4f97e08deb07e61fe35d0c16de0fc2313aa245c5ffeac5b75ba04f716c3b17f135d9096dc800&payment_channel=wallet_app&ewt=eyJ0eXAiOiJFV1QifQ.eyJpc3MiOiIweDliODBhMGQ3ODVlZTgwNDZiYzFhMDU0NWI4MDYyN2ZlODZhMzJlYmEiLCJleHAiOjE3MzI2NDcxOTB9.66076da43d6b1ed7e03435de4fab8793bd5b12d14f5e88afb9fd65311926e97a75d46502df98167d5aa73623af4598070788cd7adcdc5e6af9b5fe7561c2d73901"

  val baseWebViewPaymentUrl = "https://wallet.dev.appcoins.io/iap"

  private fun buildWebViewPaymentUrl(transaction: TransactionBuilder): Single<String> {
    return Single.zip(
      walletService.getAndSignCurrentWalletAddress(),
      ewtObtainer.getEwtAuthenticationNoBearer().subscribeOn(rxSchedulers.io) // TODO confirmar wallet usada
    ) { walletModel, ewt ->
      Pair(walletModel, ewt)
    }
      .map { pair ->
        val walletModel = pair.first
        val ewt = pair.second

        "$baseWebViewPaymentUrl?" +
            "referrer_url=${URLEncoder.encode(transaction.referrerUrl, StandardCharsets.UTF_8.toString())}" +
            "&country=PT" + //TODO
            "&address=${walletModel.address}" +
            "&signature=${walletModel.signedAddress}" +
            "&payment_channel=wallet_app" + // TODO
            "&ewt=${ewt}"
      }
  }

  private fun startOneStepWithError(errorFromReceiver: String?) {
    val intent =
      newIntent(this, intent, errorFromReceiver)
    @Suppress("DEPRECATION")
    startActivityForResult(intent, REQUEST_CODE)
  }

  private fun launchWebViewPayment(url: String) {
    val intentWebView = Intent(this, WebViewPaymentActivity::class.java).apply {
      putExtra("url", url)
    }
    startActivity(intentWebView)
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