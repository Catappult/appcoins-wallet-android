package com.asfoundation.wallet.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.walletservices.WalletService
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.ActivityIabWalletCreationBinding
import com.asfoundation.wallet.analytics.SaveIsFirstPaymentUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.PRODUCT_NAME
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.newIntent
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.webview_payment.WebViewPaymentActivity
import com.asfoundation.wallet.ui.webview_payment.usecases.CreateWebViewPaymentSdkUseCase
import com.asfoundation.wallet.ui.webview_payment.usecases.IsWebViewPaymentFlowUseCase
import com.asfoundation.wallet.util.TransferParser
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by trinkes on 13/03/2018.
 */
@AndroidEntryPoint
class Erc681Receiver : BaseActivity(), Erc681ReceiverView {
  @Inject
  lateinit var walletService: WalletService

  @Inject
  lateinit var transferParser: TransferParser

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var analytics: PaymentMethodsAnalytics

  @Inject
  lateinit var createWebViewPaymentSdkUseCase: CreateWebViewPaymentSdkUseCase

  @Inject
  lateinit var isWebViewPaymentFlowUseCase: IsWebViewPaymentFlowUseCase

  @Inject
  lateinit var setIsFirstPaymentUseCase: SaveIsFirstPaymentUseCase

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var partnerAddressService: PartnerAddressService

  @Inject
  lateinit var rxSchedulers: RxSchedulers

  @Inject
  lateinit var billingAnalytics: BillingAnalytics

  private lateinit var presenter: Erc681ReceiverPresenter

  private val binding by viewBinding(ActivityIabWalletCreationBinding::bind)

  companion object {
    const val REQUEST_CODE = 234
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState == null) analytics.startTimingForSdkTotalEvent()
    setContentView(R.layout.activity_iab_wallet_creation)
    val productName = intent.extras?.getString(PRODUCT_NAME, "")
    presenter =
      Erc681ReceiverPresenter(
        view = this,
        transferParser = transferParser,
        inAppPurchaseInteractor = inAppPurchaseInteractor,
        walletService = walletService,
        data = intent.dataString!!,
        viewScheduler = AndroidSchedulers.mainThread(),
        disposables = CompositeDisposable(),
        productName = productName,
        partnerAddressService = partnerAddressService,
        createWebViewPaymentSdkUseCase = createWebViewPaymentSdkUseCase,
        isWebViewPaymentFlowUseCase = isWebViewPaymentFlowUseCase,
        setIsFirstPaymentUseCase = setIsFirstPaymentUseCase,
        rxSchedulers = rxSchedulers,
        billingAnalytics = billingAnalytics,
        addressService = partnerAddressService,
        logger = logger,
        appVersionCode = BuildConfig.VERSION_CODE,
        context = this,
      )

    CoroutineScope(Dispatchers.Main).launch {
      presenter.present(savedInstanceState)
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

  override fun getCallingPackage(): String? = super.getCallingPackage()

  override fun startEipTransfer(transactionBuilder: TransactionBuilder, isBds: Boolean) {
    val intent: Intent = if (intent.data != null && intent.data.toString()
        .contains("/buy?")
    ) {
      newIntent(this, intent, transactionBuilder, isBds, transactionBuilder.payload)
    } else {
      SendActivity.newIntent(this, intent)
    }
    @Suppress("DEPRECATION")
    startActivityForResult(intent, REQUEST_CODE)
  }

  override fun startApp(throwable: Throwable) {
    logger.log("Erc681Receiver", throwable)
    throwable.printStackTrace()
    startActivity(MainActivity.newIntent(this, supportNotificationClicked = false))
    finish()
  }

  override fun launchWebViewPayment(url: String, transaction: TransactionBuilder, type: String) {
    val intentWebView = Intent(this, WebViewPaymentActivity::class.java).apply {
      putExtra(WebViewPaymentActivity.URL, url)
      putExtra(WebViewPaymentActivity.TRANSACTION_BUILDER, transaction)
      putExtra(WebViewPaymentActivity.TYPE, type)
    }
    @Suppress("DEPRECATION")
    startActivityForResult(intentWebView, OneStepPaymentReceiver.REQUEST_CODE)
  }

  override fun endAnimation() {
    binding.createWalletAnimation.visibility = View.INVISIBLE
    binding.createWalletText.visibility = View.INVISIBLE
    binding.createWalletCard.visibility = View.INVISIBLE
    binding.createWalletAnimation.removeAllAnimatorListeners()
    binding.createWalletAnimation.removeAllUpdateListeners()
    binding.createWalletAnimation.removeAllLottieOnCompositionLoadedListener()
  }

  override fun showLoadingAnimation() {
    binding.createWalletAnimation.visibility = View.VISIBLE
    binding.createWalletCard.visibility = View.VISIBLE
    binding.createWalletText.visibility = View.VISIBLE
    binding.createWalletAnimation.playAnimation()
  }

  override fun onPause() {
    presenter.pause()
    super.onPause()
  }
}