package com.asfoundation.wallet.verification

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.verification.code.WalletVerificationCodeFragment
import com.asfoundation.wallet.verification.intro.WalletVerificationIntroFragment

class WalletVerificationActivityNavigator(private val context: Context,
                                          private val fragmentManager: FragmentManager) {

  fun navigateToWalletVerificationIntro() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            WalletVerificationIntroFragment.newInstance())
        .commit()
  }

  fun navigateToWalletVerificationCode() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            WalletVerificationCodeFragment.newInstance())
        .commit()
  }

  fun finish() {

  }

}
