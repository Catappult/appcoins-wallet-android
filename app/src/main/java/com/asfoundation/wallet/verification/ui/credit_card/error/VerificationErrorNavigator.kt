package com.asfoundation.wallet.verification.ui.credit_card.error

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityNavigator
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityView
import javax.inject.Inject

class VerificationErrorNavigator @Inject constructor(private val fragmentManager: FragmentManager,
                                 fragment: Fragment,
                                 private val activityNavigator: VerificationCreditCardActivityNavigator) {

  private val activityView = fragment.activity as VerificationCreditCardActivityView

  fun navigateToInitialWalletVerification() {
    for (i in 0 until fragmentManager.backStackEntryCount) {
      fragmentManager.popBackStack()
    }
    activityNavigator.navigateToWalletVerificationIntro()
  }

  fun navigateToCodeWalletVerification() = fragmentManager.popBackStack()

  fun cancel() = activityView.cancel()
}