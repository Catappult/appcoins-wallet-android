package com.asfoundation.wallet.wallet_verification.code

import androidx.fragment.app.FragmentManager

class WalletVerificationCodeNavigator(private val fragmentManager: FragmentManager) {

  fun navigateToInitialWalletVerification() {
    fragmentManager.popBackStack()
  }

  fun cancel() {

  }

  fun finish() {

  }

}
