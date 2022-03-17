package com.asfoundation.wallet.recover

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecoverActivity : BaseActivity() {

  companion object {
    @JvmStatic
    fun newIntent(context: Context) =
      Intent(context, RecoverActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.recover_wallet_settings_activity)
    toolbar()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      if (supportFragmentManager.backStackEntryCount >= 1) {
        supportFragmentManager.popBackStack()
      } else {
        finish()
      }
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}