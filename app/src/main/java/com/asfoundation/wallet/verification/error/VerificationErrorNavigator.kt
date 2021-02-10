package com.asfoundation.wallet.verification.error

import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.verification.VerificationActivityNavigator
import com.asfoundation.wallet.verification.VerificationActivityView

class VerificationErrorNavigator(private val fragmentManager: FragmentManager,
                                 private val activityView: VerificationActivityView,
                                 private val activityNavigator: VerificationActivityNavigator) {

  fun navigateToInitialWalletVerification() {
    for (i in 0 until fragmentManager.backStackEntryCount) {
      fragmentManager.popBackStack()
    }
    activityNavigator.navigateToWalletVerificationIntro()
  }

  fun navigateToCodeWalletVerification() = fragmentManager.popBackStack()

  fun cancel() = activityView.cancel()
}