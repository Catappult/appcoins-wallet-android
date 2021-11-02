package com.asfoundation.wallet.verification.credit_card.error

import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.verification.credit_card.VerificationCreditCardActivityNavigator
import com.asfoundation.wallet.verification.credit_card.VerificationCreditCardActivityView

class VerificationErrorNavigator(private val fragmentManager: FragmentManager,
                                 private val activityView: VerificationCreditCardActivityView,
                                 private val activityNavigator: VerificationCreditCardActivityNavigator) {

  fun navigateToInitialWalletVerification() {
    for (i in 0 until fragmentManager.backStackEntryCount) {
      fragmentManager.popBackStack()
    }
    activityNavigator.navigateToWalletVerificationIntro()
  }

  fun navigateToCodeWalletVerification() = fragmentManager.popBackStack()

  fun cancel() = activityView.cancel()
}