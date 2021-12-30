package com.asfoundation.wallet.verification.ui.paypal

import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationPaypalActivity : BaseActivity() {

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
}