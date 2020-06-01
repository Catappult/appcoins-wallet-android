package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.billing.analytics.WalletEventSender
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics

class RestoreWalletActivityPresenter(private val walletEventSender: WalletEventSender) {

  var currentFragment: String = RestoreWalletFragment.javaClass.name

  private fun sendWalletImportRestoreBackEvent() {
    walletEventSender.sendWalletImportRestoreEvent(WalletsAnalytics.ACTION_BACK,
        WalletsAnalytics.STATUS_FAIL, WalletsAnalytics.REASON_CANCELED)
  }

  private fun sendWalletPasswordRestoreBackEvent() {
    walletEventSender.sendWalletPasswordRestoreEvent(WalletsAnalytics.ACTION_BACK,
        WalletsAnalytics.STATUS_FAIL, WalletsAnalytics.REASON_CANCELED)
  }

  fun sendBackEvent() {
    when (currentFragment) {
      RestoreWalletFragment.javaClass.name -> sendWalletImportRestoreBackEvent()
      RestoreWalletPasswordFragment.javaClass.name -> sendWalletPasswordRestoreBackEvent()
    }
  }
}