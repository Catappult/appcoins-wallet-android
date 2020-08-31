package com.asfoundation.wallet.topup

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupNotificationUtils
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.topup.payment.AdyenTopUpFragment
import com.asfoundation.wallet.transactions.PerkBonusService
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationActivity
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.error_top_up_layout.*
import kotlinx.android.synthetic.main.support_error_layout.error_message
import kotlinx.android.synthetic.main.support_error_layout.layout_support_icn
import kotlinx.android.synthetic.main.support_error_layout.layout_support_logo
import kotlinx.android.synthetic.main.top_up_activity_layout.*
import java.util.*
import javax.inject.Inject

class TopUpActivity : BaseActivity(), TopUpActivityView, ToolbarManager, UriNavigator {

  @Inject
  lateinit var topUpInteractor: TopUpInteractor

  @Inject
  lateinit var topUpAnalytics: TopUpAnalytics

  @Inject
  lateinit var walletBlockedInteract: WalletBlockedInteract

  private lateinit var results: PublishRelay<Uri>
  private lateinit var presenter: TopUpActivityPresenter
  private var isFinishingPurchase = false
  private var firstImpression = true

  companion object {
    @JvmStatic
    fun newIntent(context: Context) = Intent(context, TopUpActivity::class.java)

    const val WEB_VIEW_REQUEST_CODE = 1234
    const val WALLET_VALIDATION_REQUEST_CODE = 1235
    const val ERROR_MESSAGE = "error_message"
    private const val TOP_UP_AMOUNT = "top_up_amount"
    private const val TOP_UP_CURRENCY = "currency"
    private const val TOP_UP_CURRENCY_SYMBOL = "currency_symbol"
    private const val BONUS = "bonus"
    private const val FIRST_IMPRESSION = "first_impression"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.top_up_activity_layout)
    presenter = TopUpActivityPresenter(this, topUpInteractor, AndroidSchedulers.mainThread(),
        Schedulers.io(), CompositeDisposable())
    results = PublishRelay.create()
    presenter.present(savedInstanceState == null)
    if (savedInstanceState != null && savedInstanceState.containsKey(FIRST_IMPRESSION)) {
      firstImpression = savedInstanceState.getBoolean(FIRST_IMPRESSION)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.processActivityResult(requestCode, resultCode, data)
  }

  override fun showTopUpScreen() {
    toolbar()
    handleTopUpStartAnalytics()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, TopUpFragment.newInstance(packageName))
        .commit()
    layout_error.visibility = View.GONE
    fragment_container.visibility = View.VISIBLE
  }

  override fun showWalletValidation(@StringRes error: Int) {
    fragment_container.visibility = View.GONE
    val intent = WalletValidationActivity.newIntent(this, error)
        .apply {
          intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    startActivityForResult(intent, WALLET_VALIDATION_REQUEST_CODE)
  }

  override fun showError(@StringRes error: Int) {
    layout_error.visibility = View.VISIBLE
    error_message.text = getText(error)
  }

  override fun getSupportClicks(): Observable<Any> {
    return Observable.merge(RxView.clicks(layout_support_logo), RxView.clicks(layout_support_icn))
  }

  override fun navigateToAdyenPayment(paymentType: PaymentType, data: TopUpPaymentData) {
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container,
            AdyenTopUpFragment.newInstance(paymentType, data))
        .addToBackStack(AdyenTopUpFragment::class.java.simpleName)
        .commit()
  }

  override fun navigateToLocalPayment(paymentId: String, icon: String, label: String,
                                      topUpData: TopUpPaymentData) {
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container,
            LocalTopUpPaymentFragment.newInstance(paymentId, icon, label, topUpData))
        .addToBackStack(LocalTopUpPaymentFragment::class.java.simpleName)
        .commit()
  }

  override fun onBackPressed() {
    when {
      isFinishingPurchase -> close()
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

  override fun navigateBack() {
    if (supportFragmentManager.backStackEntryCount != 0) {
      supportFragmentManager.popBackStack()
    } else {
      close()
    }
  }

  override fun setupToolbar() {
    toolbar()
  }

  override fun popBackStack() {
    if (supportFragmentManager.backStackEntryCount != 0) {
      supportFragmentManager.popBackStack()
    }
  }

  override fun launchPerkBonusService(address: String) {
    PerkBonusService.buildService(this, address)
  }

  override fun finishActivity(data: Bundle) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            TopUpSuccessFragment.newInstance(data.getString(TOP_UP_AMOUNT, ""),
                data.getString(TOP_UP_CURRENCY, ""), data.getString(BONUS, ""),
                data.getString(TOP_UP_CURRENCY_SYMBOL, "")),
            TopUpSuccessFragment::class.java.simpleName)
        .commit()
    unlockRotation()
  }

  override fun showBackupNotification(walletAddress: String) {
    BackupNotificationUtils.showBackupNotification(this, walletAddress)
  }

  override fun finish(data: Bundle) {
    if (data.getInt(AppcoinsBillingBinder.RESPONSE_CODE) == AppcoinsBillingBinder.RESULT_OK) {
      presenter.handleBackupNotifications(data)
    } else {
      finishActivity(data)
    }
  }

  override fun close(navigateToTransactions: Boolean) {
    if (supportFragmentManager.findFragmentByTag(
            TopUpSuccessFragment::class.java.simpleName) != null && navigateToTransactions) {
      TransactionsRouter().open(this, true)
    }
    finish()
  }

  override fun acceptResult(uri: Uri) {
    results.accept(Objects.requireNonNull(uri, "Intent data cannot be null!"))
  }

  override fun navigateToUri(url: String) {
    startActivityForResult(WebViewActivity.newIntent(this, url), WEB_VIEW_REQUEST_CODE)
  }

  override fun showToolbar() = setupToolbar()

  override fun uriResults() = results

  override fun unlockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun lockOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun getTryAgainClicks() = RxView.clicks(try_again)

  override fun setFinishingPurchase() {
    isFinishingPurchase = true
  }

  override fun cancelPayment() {
    if (supportFragmentManager.backStackEntryCount != 0) {
      supportFragmentManager.popBackStackImmediate()
    } else {
      super.onBackPressed()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putBoolean(FIRST_IMPRESSION, firstImpression)
  }

  private fun handleTopUpStartAnalytics() {
    if (firstImpression) {
      topUpAnalytics.sendStartEvent()
      firstImpression = false
    }
  }

}
