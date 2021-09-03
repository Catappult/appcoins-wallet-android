package com.asfoundation.wallet.verification.code

import androidx.fragment.app.FragmentManager
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asf.wallet.R
import com.asfoundation.wallet.verification.VerificationActivityNavigator
import com.asfoundation.wallet.verification.VerificationActivityView
import com.asfoundation.wallet.verification.error.VerificationErrorFragment

class VerificationCodeNavigator(private val fragmentManager: FragmentManager,
                                private val activityView: VerificationActivityView,
                                private val activityNavigator: VerificationActivityNavigator) {

  fun navigateToInitialWalletVerification() {
    for (i in 0 until fragmentManager.backStackEntryCount) {
      fragmentManager.popBackStack()
    }
    activityNavigator.navigateToWalletVerificationIntro()
  }

  fun cancel() = activityView.cancel()

  fun finish() = activityView.complete()

  fun navigateToError(errorType: VerificationCodeResult.ErrorType, amount: String?,
                      symbol: String?) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            VerificationErrorFragment.newInstance(errorType, amount, symbol))
        .commit()
  }
}
