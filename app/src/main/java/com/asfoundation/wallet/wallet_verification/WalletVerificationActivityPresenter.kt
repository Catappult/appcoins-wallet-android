package com.asfoundation.wallet.wallet_verification

import android.os.Bundle

class WalletVerificationActivityPresenter(
    private val navigator: WalletVerificationActivityNavigator) {


  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) navigator.navigateToInitialWalletVerification()
  }

}