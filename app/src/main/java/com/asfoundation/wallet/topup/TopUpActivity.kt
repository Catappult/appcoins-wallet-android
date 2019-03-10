package com.asfoundation.wallet.topup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
import com.asfoundation.wallet.topup.payment.PaymentAuthFragment
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import java.math.BigDecimal

class TopUpActivity : BaseActivity(), TopUpActivityView, ToolbarManager, UriNavigator {

  private lateinit var results: PublishRelay<Uri>

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, TopUpActivity::class.java)
    }
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.top_up_activity_layout)
    TopUpActivityPresenter(this).present(savedInstanceState == null)
    results = PublishRelay.create()
  }

  override fun showTopUpScreen() {
    setupToolbar()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, TopUpFragment.newInstance()).commit()
  }

  override fun navigateToAdyen(isBds: Boolean, currency: String,
                               paymentType: PaymentType) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PaymentAuthFragment.newInstance(
                paymentType,
                "com.appcoins.trivialdrivesample.test",
                "https://apichain-dev.blockchainds.com/transaction/inapp?product=gas&domain=com.appcoins.trivialdrivesample.test",
                BigDecimal("1"),
                currency))
        .commit()
  }

  override fun setupToolbar() {
    setTitle(R.string.topup_title)
    toolbar()
  }

  override fun showError() {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun navigateToUri(url: String?) {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun finish(data: Bundle) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            TopUpSuccessFragment.newInstance(
                1.0))
        .commit()
  }

  override fun close() {
    finish()
  }

  override fun navigateToAdyenAuthorization(isBds: Boolean, currency: String,
                                            paymentType: PaymentType) {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun navigateToWebViewAuthorization(url: String) {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun uriResults(): Observable<Uri> {
    return results
  }
}
