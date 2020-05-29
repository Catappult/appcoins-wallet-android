package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.billing.analytics.WalletAnalytics
import com.asfoundation.wallet.billing.analytics.WalletEventSender

class RestoreWalletActivityPresenter(private val walletEventSender: WalletEventSender) {

  fun sendBackEvent() {
    walletEventSender.sendWalletImportRestoreEvent(WalletAnalytics.ACTION_BACK,
        WalletAnalytics.STATUS_FAIL, WalletAnalytics.REASON_CANCELED)
  }
}