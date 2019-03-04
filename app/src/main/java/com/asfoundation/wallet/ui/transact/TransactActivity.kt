package com.asfoundation.wallet.ui.transact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import java.math.BigDecimal

class TransactActivity : BaseActivity(), TransactActivityView, TransactNavigator {
  private lateinit var presenter: TransactActivityPresenter

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, TransactActivity::class.java)
    }
  }

  override fun closeScreen() {
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.transaction_activity_layout)
    presenter = TransactActivityPresenter(this)
    presenter.present(savedInstanceState == null)
    toolbar()
  }

  override fun showTransactFragment() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, TransactFragment.newInstance()).commit()
  }

  override fun showLoading() {
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
  }

  override fun openAppcoinsCreditsSuccess(walletAddress: String,
                                          amount: BigDecimal, currency: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            AppcoinsCreditsTransactSuccessFragment.newInstance(amount, currency, walletAddress))
        .commit()
  }
}