package com.asfoundation.wallet.restore

import android.os.Bundle
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.restore.intro.RestoreWalletFragment
import com.asfoundation.wallet.restore.password.RestoreWalletPasswordFragment

class RestoreWalletActivityPresenter(private val view: RestoreWalletActivityView,
                                     private val walletsEventSender: WalletsEventSender,
                                     private val navigator: RestoreWalletActivityNavigator) {


  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) navigator.navigateToInitialRestoreFragment()
  }

  private fun sendWalletRestoreBackEvent() {
    walletsEventSender.sendWalletRestoreEvent(WalletsAnalytics.ACTION_BACK,
        WalletsAnalytics.STATUS_FAIL, WalletsAnalytics.REASON_CANCELED)
  }

  private fun sendWalletPasswordRestoreBackEvent() {
    walletsEventSender.sendWalletPasswordRestoreEvent(WalletsAnalytics.ACTION_BACK,
        WalletsAnalytics.STATUS_FAIL, WalletsAnalytics.REASON_CANCELED)
  }

  fun sendBackEvent() {
    when (view.getCurrentFragment()) {
      RestoreWalletFragment::class.java.simpleName -> sendWalletRestoreBackEvent()
      RestoreWalletPasswordFragment::class.java.simpleName -> sendWalletPasswordRestoreBackEvent()
    }
  }

  fun onAnimationEnd() {
    navigator.navigateToTransactions()
    view.endActivity()
  }
}