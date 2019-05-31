package com.asfoundation.wallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.router.TransactionsRouter
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_balance.*

class BalanceActivity : BaseActivity(), BalanceActivityView {

  private lateinit var presenter: BalancePresenter

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, BalanceActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_balance)
    presenter = BalancePresenter(this)
  }

  override fun onResume() {
    super.onResume()
    presenter.present()
  }

  override fun onBackPressed() {
    TransactionsRouter().open(this, true)
    finish()
  }

  override fun showBalanceScreen() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BalanceFragment.newInstance()).commit()
  }

  override fun showTokenDetailsScreen(tokenId: String) {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showTopUpScreen() {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun setupToolbar() {
    toolbar()
  }
}