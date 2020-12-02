package com.asfoundation.wallet.verification.code

import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.verification.WalletVerificationActivityView

class WalletVerificationCodeNavigator(private val fragmentManager: FragmentManager,
                                      private val activityView: WalletVerificationActivityView) {

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
