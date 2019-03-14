package com.asfoundation.wallet.topup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
import com.asfoundation.wallet.topup.payment.PaymentAuthFragment
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.WebViewActivity
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.AndroidInjection
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class TopUpActivity : BaseActivity(), TopUpActivityView, ToolbarManager, UriNavigator {

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  private lateinit var results: PublishRelay<Uri>
  private lateinit var transactionData: String

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, TopUpActivity::class.java)
    }

    private const val WEB_VIEW_REQUEST_CODE = 1234
    private const val TOP_UP_AMOUNT = "top_up_amount"
    val LOCAL_CURRENCY = "LOCAL_CURRENCY"
    val APPC_C = "APPC_C"
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.top_up_activity_layout)
    TopUpActivityPresenter(this).present(savedInstanceState == null)
    results = PublishRelay.create()

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == WEB_VIEW_REQUEST_CODE) {
      if (resultCode == WebViewActivity.FAIL) {
        finish()
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    results.accept(Objects.requireNonNull(intent.data, "Intent data cannot be null!"))
  }

  override fun showTopUpScreen() {
    setupToolbar()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, TopUpFragment.newInstance(packageName)).commit()
  }

  override fun navigateToPayment(paymentType: PaymentType, data: TopUpData,
                                 selectedCurrency: String, transaction: TransactionBuilder) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PaymentAuthFragment.newInstance(
                paymentType,
                transaction,
                data,
                selectedCurrency))
        .commit()
  }

  override fun setupToolbar() {
    setTitle(R.string.topup_title)
    toolbar()
  }

  override fun finish(data: Bundle) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            TopUpSuccessFragment.newInstance(data.getDouble(TOP_UP_AMOUNT)))
        .commit()
  }

  override fun close() {
    finish()
  }

  override fun navigateToUri(url: String?, transaction: TransactionBuilder) {
    startActivityForResult(WebViewActivity.newIntent(this, url, transaction, this),
        WEB_VIEW_REQUEST_CODE)
  }

  override fun uriResults(): Observable<Uri> {
    return results
  }

  override fun getActivityIntent(): Intent {
    return TopUpActivity.newIntent(this)
  }
}
