package com.asfoundation.wallet.verification.paypal

import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.verification.paypal.intro.VerificationPaypalIntroFragment
import dagger.android.AndroidInjection

class VerificationPaypalActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_paypal_verification)

    setTitle(R.string.verification_settings_unverified_title)
    toolbar()

    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            VerificationPaypalIntroFragment.newInstance())
        .commit()
  }
}