package com.asfoundation.wallet.verification.ui.paypal

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.wallet.appcoins.core.legacy_base.legacy.BaseActivity
import com.asf.wallet.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationPaypalActivity : com.wallet.appcoins.core.legacy_base.legacy.BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_paypal_verification)

    setTitle(R.string.verification_settings_unverified_title)
    toolbar()

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .replace(R.id.fragment_container,
              VerificationPaypalFragment.newInstance())
          .commit()
    }
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
}