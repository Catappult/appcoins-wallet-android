package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender

class RestoreWalletActivityPresenter(private val walletsEventSender: WalletsEventSender) {

  var currentFragment: String = RestoreWalletFragment::class.java.simpleName

  private fun sendWalletImportRestoreBackEvent() {
    walletsEventSender.sendWalletImportRestoreEvent(WalletsAnalytics.ACTION_BACK,
        WalletsAnalytics.STATUS_FAIL, WalletsAnalytics.REASON_CANCELED)
  }

  private fun sendWalletPasswordRestoreBackEvent() {
    walletsEventSender.sendWalletPasswordRestoreEvent(WalletsAnalytics.ACTION_BACK,
        WalletsAnalytics.STATUS_FAIL, WalletsAnalytics.REASON_CANCELED)
  }

  fun sendBackEvent() {
    when (currentFragment) {
      RestoreWalletFragment::class.java.simpleName -> sendWalletImportRestoreBackEvent()
      RestoreWalletPasswordFragment::class.java.simpleName -> sendWalletPasswordRestoreBackEvent()
    }
  }
}