package com.asfoundation.wallet.topup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.topup.payment.PaymentAuthFragment
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.AndroidInjection
import io.reactivex.Observable
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class TopUpActivity : BaseActivity(), TopUpActivityView, ToolbarManager, UriNavigator {

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  private lateinit var results: PublishRelay<Uri>
  private lateinit var presenter: TopUpActivityPresenter

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, TopUpActivity::class.java)
    }

    fun newIntent(context: Context, url: String): Intent {
      val intent = Intent(context, TopUpActivity::class.java)
      intent.data = Uri.parse(url)
      return intent
    }

    const val WEB_VIEW_REQUEST_CODE = 1234
    private const val TOP_UP_AMOUNT = "top_up_amount"
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.top_up_activity_layout)
    presenter = TopUpActivityPresenter(this)
    presenter.present(savedInstanceState == null)
    results = PublishRelay.create()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.processActivityResult(requestCode, resultCode, data)
  }

  override fun showTopUpScreen() {
    setupToolbar()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, TopUpFragment.newInstance(packageName)).commit()
  }

  override fun onBackPressed() {
    close()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        close()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun navigateToPayment(paymentType: PaymentType, data: TopUpData,
                                 selectedCurrency: String, origin: String, transactionType: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PaymentAuthFragment.newInstance(
                paymentType,
                data,
                selectedCurrency,
                origin,
                transactionType))
        .commit()
  }

  override fun setupToolbar() {
    toolbar()
  }

  override fun finish(data: Bundle) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            TopUpSuccessFragment.newInstance(data.getDouble(TOP_UP_AMOUNT)))
        .commit()
  }

  override fun close() {
    TransactionsRouter().open(this, true)
    finish()
  }

  override fun acceptResult(uri: Uri) {
    results.accept(Objects.requireNonNull(uri, "Intent data cannot be null!"))
  }

  override fun navigateToUri(url: String, domain: String, skuId: String, amount: BigDecimal,
                             type: String) {
    startActivityForResult(WebViewActivity.newIntent(this, url, domain, skuId, amount, type),
        WEB_VIEW_REQUEST_CODE)
  }

  override fun showToolbar() {
    setupToolbar()
  }

  override fun uriResults(): Observable<Uri> {
    return results
  }
}
