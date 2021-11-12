package com.asfoundation.wallet.verification.ui.credit_card

import android.app.Activity
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.verification.ui.credit_card.code.VerificationCodeFragment
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationIntroFragment

class VerificationCreditCardActivityNavigator(private val activity: Activity,
                                              private val fragmentManager: FragmentManager) {

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

  fun finish() = activity.finish()

  fun backPress() = activity.onBackPressed()

  fun navigateToWalletVerificationIntroNoStack() {
    for (i in 0 until fragmentManager.backStackEntryCount) {
      fragmentManager.popBackStack()
    }
    navigateToWalletVerificationIntro()
  }
}
