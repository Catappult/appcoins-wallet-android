package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.billing.analytics.WalletAnalytics
import com.asfoundation.wallet.billing.analytics.WalletEventSender

class RestoreWalletActivityPresenter(private val walletEventSender: WalletEventSender) {

  var currentFragment: String = RestoreWalletFragment.javaClass.name

  private fun sendWalletImportRestoreBackEvent() {
    walletEventSender.sendWalletImportRestoreEvent(WalletAnalytics.ACTION_BACK,
        WalletAnalytics.STATUS_FAIL, WalletAnalytics.REASON_CANCELED)
  }

  private fun sendWalletPasswordRestoreBackEvent() {
    walletEventSender.sendWalletPasswordRestoreEvent(WalletAnalytics.ACTION_BACK,
        WalletAnalytics.STATUS_FAIL, WalletAnalytics.REASON_CANCELED)
  }

  fun sendBackEvent() {
    when (currentFragment) {
      RestoreWalletFragment.javaClass.name -> sendWalletImportRestoreBackEvent()
      RestoreWalletPasswordFragment.javaClass.name -> sendWalletPasswordRestoreBackEvent()
    }
  }
}