package com.asfoundation.wallet.verification.credit_card.code

import androidx.fragment.app.FragmentManager
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asf.wallet.R
import com.asfoundation.wallet.verification.credit_card.VerificationCreditCardActivityNavigator
import com.asfoundation.wallet.verification.credit_card.VerificationCreditCardActivityView
import com.asfoundation.wallet.verification.credit_card.error.VerificationErrorFragment

class VerificationCodeNavigator(private val fragmentManager: FragmentManager,
                                private val activityView: VerificationCreditCardActivityView,
                                private val activityNavigator: VerificationCreditCardActivityNavigator) {

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
