package com.asfoundation.wallet.wallet_verification

import android.os.Bundle
import com.asfoundation.wallet.billing.analytics.WalletsEventSender

class WalletVerificationActivityPresenter(private val view: WalletVerificationActivityView,
                                          private val walletsEventSender: WalletsEventSender,
                                          private val navigator: WalletVerificationActivityNavigator) {


  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) return
  }

  fun sendBackEvent() {
  }
}