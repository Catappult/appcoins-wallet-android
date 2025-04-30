package com.asfoundation.wallet.topup

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.compose.ui.platform.ComposeView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.ui.widgets.top_bar.TopBar
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.TopUpActivityLayoutBinding
import com.asfoundation.wallet.backup.BackupNotificationUtils
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.googlepay.GooglePayTopupFragment
import com.asfoundation.wallet.billing.paypal.PayPalTopupFragment
import com.asfoundation.wallet.billing.true_layer.TrueLayerTopupFragment
import com.asfoundation.wallet.gamification.UpdateUserStatsUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.promotions.usecases.StartVipReferralPollingUseCase
import com.asfoundation.wallet.topup.adyen.AdyenTopUpFragment
import com.asfoundation.wallet.topup.amazonPay.AmazonPayTopUpFragment
import com.asfoundation.wallet.topup.localpayments.LocalTopUpPaymentFragment
import com.asfoundation.wallet.topup.vkPayment.VkPaymentTopUpFragment
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationService
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.Objects
import javax.inject.Inject


@AndroidEntryPoint
class TopUpActivity : BaseActivity(), TopUpActivityView, UriNavigator {

  @Inject
  lateinit var topUpInteractor: TopUpInteractor

  @Inject
  lateinit var updateUserStatsUseCase: UpdateUserStatsUseCase

  @Inject
  lateinit var startVipReferralPollingUseCase: StartVipReferralPollingUseCase

  @Inject
  lateinit var topUpAnalytics: TopUpAnalytics

  @Inject
  lateinit var walletBlockedInteract: WalletBlockedInteract

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var displayChatUseCase: DisplayChatUseCase

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  private lateinit var results: PublishRelay<Uri>
  private lateinit var presenter: TopUpActivityPresenter
  private var isFinishingPurchase = false
  private var firstImpression = true

  private val views by viewBinding(TopUpActivityLayoutBinding::bind)

  companion object {
    @JvmStatic
    fun newIntent(context: Context) = Intent(context, TopUpActivity::class.java)

    const val WEB_VIEW_REQUEST_CODE = 1234
    private const val TOP_UP_AMOUNT = "top_up_amount"
    private const val TOP_UP_CURRENCY = "currency"
    private const val TOP_UP_CURRENCY_SYMBOL = "currency_symbol"
    private const val BONUS = "bonus"
    private const val FIRST_IMPRESSION = "first_impression"
    private const val PENDING_FINAL_CONFIRMATION = "pending_final_confirmation"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.top_up_activity_layout)
    presenter = TopUpActivityPresenter(
      view = this,
      topUpInteractor = topUpInteractor,
      startVipReferralPollingUseCase = startVipReferralPollingUseCase,
      viewScheduler = AndroidSchedulers.mainThread(),
      networkScheduler = Schedulers.io(),
      disposables = CompositeDisposable(),
      logger = logger,
      displayChatUseCase = displayChatUseCase,
      updateUserStatsUseCase = updateUserStatsUseCase,
    )
    results = PublishRelay.create()
    presenter.present(savedInstanceState == null)
    if (savedInstanceState != null && savedInstanceState.containsKey(FIRST_IMPRESSION)) {
      firstImpression = savedInstanceState.getBoolean(FIRST_IMPRESSION)
    }
    views.topBar.composeView.apply {
      setContent {
        TopBar(isMainBar = false, onClickSupport = { presenter.displayChat() }, fragmentName = fragmentName, buttonsAnalytics = buttonsAnalytics)
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.processActivityResult(requestCode, resultCode, data)
  }

  override fun showTopUpScreen() {
    handleTopUpStartAnalytics()
    supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, TopUpFragment.newInstance(packageName)).commit()
    views.layoutError.root.visibility = View.GONE
    views.fragmentContainer.visibility = View.VISIBLE
  }

  override fun showCreditCardVerification() {
    views.fragmentContainer.visibility = View.GONE
    val intent = VerificationCreditCardActivity.newIntent(this).apply {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
    startActivity(intent)
    finish()
  }

  override fun showError(@StringRes error: Int) {
    views.layoutError.root.visibility = View.VISIBLE
    views.layoutError.errorMessage.text = getText(error)
  }

  override fun getSupportClicks(): Observable<Any> {
    return Observable.merge(
      RxView.clicks(views.layoutError.layoutSupportLogo),
      RxView.clicks(views.layoutError.layoutSupportIcn)
    )
  }

  override fun navigateToAdyenPayment(
    paymentType: PaymentType, data: TopUpPaymentData, buyWithStoredCard: Boolean
  ) {
    supportFragmentManager.beginTransaction().add(
        R.id.fragment_container,
        AdyenTopUpFragment.newInstance(paymentType, data, buyWithStoredCard)
    ).addToBackStack(AdyenTopUpFragment::class.java.simpleName).commit()
  }

  override fun navigateToPaypalV2(paymentType: PaymentType, data: TopUpPaymentData) {
    supportFragmentManager.beginTransaction().add(
      R.id.fragment_container, PayPalTopupFragment.newInstance(
          paymentType = paymentType,
          data = data,
          amount = data.fiatValue,
          currency = data.fiatCurrencyCode,
          bonus = data.bonusValue.toString(),
          gamificationLevel = data.gamificationLevel,
        )
    ).addToBackStack(AdyenTopUpFragment::class.java.simpleName).commit()
  }

  override fun navigateToGooglePay(paymentType: PaymentType, data: TopUpPaymentData) {
    supportFragmentManager.beginTransaction().add(
      R.id.fragment_container, GooglePayTopupFragment.newInstance(
        paymentType = paymentType,
        data = data,
        amount = data.fiatValue,
        currency = data.fiatCurrencyCode,
        bonus = data.bonusValue.toString(),
        gamificationLevel = data.gamificationLevel,
      )
    ).addToBackStack(AdyenTopUpFragment::class.java.simpleName).commit()
  }

  override fun navigateToTrueLayer(paymentType: PaymentType, data: TopUpPaymentData) {
    supportFragmentManager.beginTransaction().add(
      R.id.fragment_container, TrueLayerTopupFragment.newInstance(
        paymentType = paymentType,
        data = data,
        amount = data.fiatValue,
        currency = data.fiatCurrencyCode,
        bonus = data.bonusValue.toString(),
        gamificationLevel = data.gamificationLevel,
      )
    ).addToBackStack(TrueLayerTopupFragment::class.java.simpleName).commit()
  }

  override fun navigateToLocalPayment(
    paymentId: String, icon: String, label: String, async: Boolean, topUpData: TopUpPaymentData
  ) {
    supportFragmentManager.beginTransaction().add(
      R.id.fragment_container, LocalTopUpPaymentFragment.newInstance(
        paymentId, icon, label, async, packageName, topUpData
        )
    ).addToBackStack(LocalTopUpPaymentFragment::class.java.simpleName).commit()
  }

  override fun navigateToPayPalVerification() {
    val intent = MainActivity.newIntent(
      this, supportNotificationClicked = false, isPayPalVerificationRequired = true
    ).apply {
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    startActivity(intent)
    finish()
  }

  override fun onBackPressed() {
    when {
      isFinishingPurchase -> close()
      views.fullscreenComposeView.visibility == View.VISIBLE -> {
//        views.fullscreenComposeView.visibility = View.GONE
        super.onBackPressed()
      }
      supportFragmentManager.backStackEntryCount != 0 -> supportFragmentManager.popBackStack()
      else -> super.onBackPressed()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      when {
        isFinishingPurchase -> close()
        supportFragmentManager.backStackEntryCount != 0 -> supportFragmentManager.popBackStack()
        else -> super.onBackPressed()
      }
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun popBackStack() {
    if (supportFragmentManager.backStackEntryCount != 0) {
      supportFragmentManager.popBackStack()
    }
  }

  override fun launchPerkBonusAndGamificationService(address: String) {
    PerkBonusAndGamificationService.buildService(this, address)
  }

  override fun navigateToVkPayPayment(topUpData: TopUpPaymentData) {
    val fragmentVk = VkPaymentTopUpFragment()
    val args = Bundle()
    args.putSerializable(VkPaymentTopUpFragment.PAYMENT_DATA, topUpData)
    fragmentVk.arguments = args
    supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragmentVk)
      .addToBackStack(VkPaymentTopUpFragment::class.java.simpleName).commit()
  }

  override fun navigateToAmazonPay(topUpData: TopUpPaymentData) {
    val fragmentVk = AmazonPayTopUpFragment()
    val args = Bundle()
    args.putSerializable(AmazonPayTopUpFragment.PAYMENT_DATA, topUpData)
    fragmentVk.arguments = args
    supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragmentVk)
      .addToBackStack(AmazonPayTopUpFragment::class.java.simpleName).commit()
  }

  override fun finishActivity(data: Bundle) {
    supportFragmentManager.beginTransaction().replace(
      R.id.fragment_container, TopUpSuccessFragment.newInstance(
          data.getString(TOP_UP_AMOUNT, ""),
          data.getString(TOP_UP_CURRENCY, ""),
          data.getString(BONUS, ""),
          data.getString(TOP_UP_CURRENCY_SYMBOL, ""),
          data.getBoolean(PENDING_FINAL_CONFIRMATION, false),
      ), TopUpSuccessFragment::class.java.simpleName
    ).commit()
    unlockRotation()
  }

  override fun showBackupNotification(walletAddress: String) {
    BackupNotificationUtils.showBackupNotification(this, walletAddress)
  }

  override fun finish(data: Bundle) {
    if (data.getInt(AppcoinsBillingBinder.RESPONSE_CODE) == AppcoinsBillingBinder.RESULT_OK) {
      presenter.handleBackupNotifications(data)
      presenter.handlePerkNotifications(data)
    } else {
      finishActivity(data)
    }
  }

  override fun close(navigateToTransactions: Boolean) {
    if (supportFragmentManager.findFragmentByTag(
        TopUpSuccessFragment::class.java.simpleName
      ) != null && navigateToTransactions
    ) {
      this.finish()
    }
    finish()
  }

  override fun acceptResult(uri: Uri) {
    results.accept(Objects.requireNonNull(uri, "Intent data cannot be null!"))
  }

  override fun navigateToUri(url: String) {
    startActivityForResult(WebViewActivity.newIntent(this, url), WEB_VIEW_REQUEST_CODE)
  }

  override fun uriResults() = results

  override fun unlockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun lockOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun getTryAgainClicks() = RxView.clicks(views.layoutError.tryAgain)

  override fun setFinishingPurchase(newState: Boolean) {
    isFinishingPurchase = newState
  }

  override fun cancelPayment() {
    if (supportFragmentManager.backStackEntryCount != 0) {
      supportFragmentManager.popBackStackImmediate()
    } else {
      super.onBackPressed()
    }
  }

  override fun getFullscreenComposeView(): ComposeView = views.fullscreenComposeView

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(FIRST_IMPRESSION, firstImpression)
  }

  override fun isActivityActive(): Boolean = !supportFragmentManager.isDestroyed

  private fun handleTopUpStartAnalytics() {
    if (firstImpression) {
      topUpAnalytics.sendStartEvent()
      firstImpression = false
    }
  }
}
