package com.asfoundation.wallet.verification.ui.credit_card

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.asf.wallet.R
import com.asfoundation.wallet.verification.ui.credit_card.code.VerificationCodeFragment
import com.asfoundation.wallet.verification.ui.credit_card.intro.VerificationIntroFragment
import javax.inject.Inject

class VerificationCreditCardActivityNavigator @Inject constructor(private val activity: Activity) {

  private val fragmentManager = (activity as AppCompatActivity).supportFragmentManager

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
