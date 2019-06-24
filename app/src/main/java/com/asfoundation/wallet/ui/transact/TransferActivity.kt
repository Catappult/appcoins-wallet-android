package com.asfoundation.wallet.ui.transact

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity
import java.math.BigDecimal

class TransferActivity : BaseActivity(), TransferActivityView, TransactNavigator {
  private lateinit var presenter: TransferActivityPresenter

  companion object {
    const val BARCODE_READER_REQUEST_CODE = 1
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, TransferActivity::class.java)
    }
  }

  override fun closeScreen() {
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.transaction_activity_layout)
    presenter = TransferActivityPresenter(this)
    presenter.present(savedInstanceState == null)
    toolbar()
  }

  override fun showTransactFragment() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, TransferFragment.newInstance()).commit()
  }

  override fun showLoading() {
    lockOrientation()
    supportFragmentManager.beginTransaction()
        .add(android.R.id.content, LoadingFragment.newInstance(),
            LoadingFragment::class.java.name).commit()
  }

  override fun hideLoading() {
    val fragment =
        supportFragmentManager.findFragmentByTag(LoadingFragment::class.java.name)
    if (fragment != null) {
      supportFragmentManager.beginTransaction().remove(fragment).commit()
    }
    unlockOrientation()
  }

  private fun lockOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
  }

  private fun unlockOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun openAppcoinsCreditsSuccess(walletAddress: String,
                                          amount: BigDecimal, currency: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            AppcoinsCreditsTransferSuccessFragment.newInstance(amount, currency, walletAddress))
        .commit()
  }

  override fun openQrCodeScreen() {
    val intent = Intent(this, BarcodeCaptureActivity::class.java)
    startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
  }

  override fun hideKeyboard() {
    val inputMethodManager =
        this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = this.currentFocus
    if (view == null) {
      view = View(this)
    }
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
  }

}