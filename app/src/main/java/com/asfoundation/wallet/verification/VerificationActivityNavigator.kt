package com.asfoundation.wallet.verification

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.verification.code.VerificationCodeFragment
import com.asfoundation.wallet.verification.intro.VerificationIntroFragment

class VerificationActivityNavigator(private val fragmentManager: FragmentManager) {

  fun navigateToWalletVerificationIntro() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            VerificationIntroFragment.newInstance())
        .commit()
  }

  fun navigateToWalletVerificationCode() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            VerificationCodeFragment.newInstance())
        .commit()
  }

  fun finish() {

  }

}
