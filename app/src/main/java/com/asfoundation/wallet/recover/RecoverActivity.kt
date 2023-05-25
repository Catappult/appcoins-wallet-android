package com.asfoundation.wallet.recover

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.asf.wallet.R
import com.google.android.material.appbar.AppBarLayout
import com.wallet.appcoins.core.legacy_base.BaseActivity
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