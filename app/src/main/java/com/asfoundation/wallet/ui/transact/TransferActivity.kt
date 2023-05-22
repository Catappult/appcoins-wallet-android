package com.asfoundation.wallet.ui.transact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.wallet.appcoins.core.legacy_base.legacy.BaseActivity
import com.asf.wallet.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransferActivity : com.wallet.appcoins.core.legacy_base.legacy.BaseActivity(), TransferActivityView, TransactNavigator {

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

  /**
   * function hardcoded temporarily, must be changed
   * @return
   */
  override fun toolbar(): Toolbar? {
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar!!.visibility = View.VISIBLE
    if (toolbar != null) {
      setSupportActionBar(toolbar)
      toolbar.title = title
    }
    enableDisplayHomeAsUp()
    return toolbar
  }

  override fun showTransactFragment() {
    supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, TransferFragment.newInstance())
      .commit()
  }
}