package com.asfoundation.wallet.verification.code

import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.verification.VerificationActivityView

class VerificationCodeNavigator(private val fragmentManager: FragmentManager,
                                private val activityView: VerificationActivityView) {

  fun navigateToInitialWalletVerification() {
    fragmentManager.popBackStack()
  }

  fun cancel() {
    activityView.cancel()
  }

  fun finish() {
    activityView.complete()
  }

}
