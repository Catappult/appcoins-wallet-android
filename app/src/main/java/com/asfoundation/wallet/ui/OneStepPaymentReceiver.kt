package com.asfoundation.wallet.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import cm.aptoide.skills.SkillsActivity
import com.airbnb.lottie.LottieAnimationView
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.service.WalletGetterStatus
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.newIntent
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.util.TransferParser
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class OneStepPaymentReceiver : BaseActivity() {
  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var walletService: WalletService

  @Inject
  lateinit var logger: com.appcoins.wallet.core.utils.jvm_common.Logger

  @Inject
  lateinit var transferParser: TransferParser

  @Inject
  lateinit var analytics: PaymentMethodsAnalytics

  private var disposable: Disposable? = null
  private var walletCreationCard: View? = null
  private var walletCreationAnimation: LottieAnimationView? = null
  private var walletCreationText: View? = null

  companion object {
    const val REQUEST_CODE = 234
    private const val ESKILLS_URI_KEY = "ESKILLS_URI"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (savedInstanceState == null) analytics.startTimingForOspTotalEvent()
    if (isEskillsUri(intent.dataString!!)) {
      val skillsActivityIntent = Intent(this, SkillsActivity::class.java)
      skillsActivityIntent.putExtra(ESKILLS_URI_KEY, intent.dataString)
      @Suppress("DEPRECATION")
      startActivityForResult(skillsActivityIntent, REQUEST_CODE)
    } else {
      setContentView(R.layout.activity_iab_wallet_creation)
      walletCreationCard = findViewById(R.id.create_wallet_card)
      walletCreationAnimation = findViewById(R.id.create_wallet_animation)
      walletCreationText = findViewById(R.id.create_wallet_text)
      if (savedInstanceState == null) {
        disposable = handleWalletCreationIfNeeded()
          .takeUntil { it != WalletGetterStatus.CREATING.toString() }
          .filter { it != WalletGetterStatus.CREATING.toString() }
          .flatMap {
            transferParser.parse(intent.dataString!!)
              .flatMap { transaction: TransactionBuilder ->
                inAppPurchaseInteractor.isWalletFromBds(transaction.domain, transaction.toAddress())
                  .doOnSuccess { isBds: Boolean ->
                    startOneStepTransfer(transaction, isBds)
                  }
              }
              .toObservable()
          }
          .subscribe({ }, { throwable: Throwable ->
            logger.log("OneStepPaymentReceiver", throwable)
            startOneStepWithError(IabActivity.ERROR_RECEIVER_NETWORK)
          })
      }
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
    .toLowerCase(Locale.ROOT)
    .contains("/transaction/eskills")

  private fun startApp(throwable: Throwable) {
    throwable.printStackTrace()
    startActivity(MainActivity.newIntent(this, supportNotificationClicked = false))
    finish()
  }

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

  private fun startOneStepWithError(errorFromReceiver: String?) {
    val intent =
      newIntent(this, intent, errorFromReceiver)
    @Suppress("DEPRECATION")
    startActivityForResult(intent, REQUEST_CODE)
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