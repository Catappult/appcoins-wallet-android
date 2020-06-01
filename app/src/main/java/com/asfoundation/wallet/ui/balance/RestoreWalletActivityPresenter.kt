package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender

class RestoreWalletActivityPresenter(private val walletsEventSender: WalletsEventSender) {

  var currentFragment: String = RestoreWalletFragment.javaClass.name

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
      RestoreWalletFragment.javaClass.name -> sendWalletImportRestoreBackEvent()
      RestoreWalletPasswordFragment.javaClass.name -> sendWalletPasswordRestoreBackEvent()
    }
  }
}