package com.asfoundation.wallet.verification.ui.credit_card.code

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asf.wallet.R
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityNavigator
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityView
import com.asfoundation.wallet.verification.ui.credit_card.error.VerificationErrorFragment
import javax.inject.Inject

class VerificationCodeNavigator @Inject constructor(private val fragmentManager: FragmentManager,
                                fragment: Fragment,
                                private val activityNavigator: VerificationCreditCardActivityNavigator) {

  private val activityView=  fragment.activity as VerificationCreditCardActivityView

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
