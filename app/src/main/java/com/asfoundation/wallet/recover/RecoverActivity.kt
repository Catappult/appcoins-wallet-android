package com.asfoundation.wallet.recover

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecoverActivity : BaseActivity() {

  companion object {
    const val ONBOARDING_LAYOUT = "onboarding_layout"

    @JvmStatic
    fun newIntent(context: Context, onboardingLayout: Boolean) =
      Intent(context, RecoverActivity::class.java).apply {
        putExtra(ONBOARDING_LAYOUT, onboardingLayout)
      }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.recover_wallet_activity)
    if (!intent.getBooleanExtra(ONBOARDING_LAYOUT, false)) {
      findViewById<AppBarLayout>(R.id.recover_wallet_app_bar).visibility = View.VISIBLE
      toolbar()
    }
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