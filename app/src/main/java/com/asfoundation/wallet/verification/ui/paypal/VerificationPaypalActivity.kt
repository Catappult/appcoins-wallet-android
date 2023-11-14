package com.asfoundation.wallet.verification.ui.paypal

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.asf.wallet.R
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationPaypalActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_paypal_verification)

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
   fun toolbar(): Toolbar? {
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