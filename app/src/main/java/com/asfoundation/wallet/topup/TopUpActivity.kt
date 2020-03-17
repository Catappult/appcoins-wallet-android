package com.asfoundation.wallet.topup

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.topup.payment.AdyenTopUpFragment
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.AndroidInjection
import java.util.*
import javax.inject.Inject

class TopUpActivity : BaseActivity(), TopUpActivityView, ToolbarManager, UriNavigator {

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  private lateinit var results: PublishRelay<Uri>
  private lateinit var presenter: TopUpActivityPresenter
  private var isFinishingPurchase = false

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, TopUpActivity::class.java)
    }

    const val WEB_VIEW_REQUEST_CODE = 1234
    private const val TOP_UP_AMOUNT = "top_up_amount"
    private const val TOP_UP_CURRENCY = "currency"
    private const val BONUS = "bonus"
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.top_up_activity_layout)
    presenter = TopUpActivityPresenter(this)
    results = PublishRelay.create()
    presenter.present(savedInstanceState == null)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.processActivityResult(requestCode, resultCode, data)
  }

  override fun showTopUpScreen() {
    setupToolbar()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, TopUpFragment.newInstance(packageName))
        .commit()
  }

  override fun navigateToPayment(paymentType: PaymentType, data: TopUpData,
                                 selectedCurrency: String, transactionType: String,
                                 bonusValue: String, defaultValues: List<FiatValue>,
                                 gamificationLevel: Int) {
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container,
            AdyenTopUpFragment.newInstance(paymentType, data, selectedCurrency,
                transactionType, bonusValue, gamificationLevel))
        .addToBackStack(AdyenTopUpFragment::class.java.simpleName)
        .commit()
  }

  override fun onBackPressed() {
    when {
      isFinishingPurchase -> close(true)
      supportFragmentManager.backStackEntryCount != 0 -> supportFragmentManager.popBackStack()
      else -> super.onBackPressed()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        when {
          isFinishingPurchase -> close(true)
          supportFragmentManager.backStackEntryCount != 0 -> supportFragmentManager.popBackStack()
          else -> super.onBackPressed()
        }
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun setupToolbar() {
    toolbar()
  }

  override fun finish(data: Bundle) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            TopUpSuccessFragment.newInstance(data.getString(TOP_UP_AMOUNT),
                data.getString(TOP_UP_CURRENCY), data.getString(BONUS)),
            TopUpSuccessFragment::class.java.simpleName)
        .commit()
    unlockRotation()
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

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    results.accept(Objects.requireNonNull(intent.data, "Intent data cannot be null!"))
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

  override fun setFinishingPurchase() {
    isFinishingPurchase = true
  }

  override fun cancelPayment() {
    if (supportFragmentManager.backStackEntryCount != 0) {
      supportFragmentManager.popBackStack()
    } else {
      super.onBackPressed()
    }
  }
}
