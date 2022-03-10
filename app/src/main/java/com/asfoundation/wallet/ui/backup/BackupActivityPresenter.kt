package com.asfoundation.wallet.ui.backup

import com.asfoundation.wallet.billing.analytics.WalletsEventSender

class BackupActivityPresenter(private val view: BackupActivityView,
                              private val data: BackupActivityData,
                              private val navigator: BackupActivityNavigator,
                              private val eventSender: WalletsEventSender) {

  fun present(isCreating: Boolean) {
    view.setupToolbar()
    if (isCreating) navigator.showBackupScreen(data.walletAddress)
  }
}
