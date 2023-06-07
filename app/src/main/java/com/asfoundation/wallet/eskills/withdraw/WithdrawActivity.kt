package com.asfoundation.wallet.eskills.withdraw

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.asf.wallet.R
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WithdrawActivity : BaseActivity() {
  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, WithdrawActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.withdraw_activity)
    toolbar()

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .add(R.id.fragment_container, WithdrawFragment.newInstance())
          .commit()
    }
  }

  /**
   * function hardcoded temporarily, must be changed
   * @return
   */
   fun toolbar(): Toolbar {
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar!!.visibility = View.VISIBLE
    if (toolbar != null) {
      setSupportActionBar(toolbar)
      toolbar.title = title
    }
    enableDisplayHomeAsUp()
    return toolbar
  }

}