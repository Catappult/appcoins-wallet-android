package com.asfoundation.wallet.ui.transact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransferActivity : BaseActivity(), TransferActivityView, TransactNavigator {

  private lateinit var presenter: TransferActivityPresenter

  companion object {

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
      .replace(R.id.fragment_container, TransferFragment.newInstance())
      .commit()
  }
}