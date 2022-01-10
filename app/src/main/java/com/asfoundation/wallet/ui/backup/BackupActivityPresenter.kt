package com.asfoundation.wallet.ui.backup

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.ui.backup.creation.BackupCreationFragment

class BackupActivityPresenter(private val view: BackupActivityView,
                              private val data: BackupActivityData,
                              private val navigator: BackupActivityNavigator,
                              private val eventSender: WalletsEventSender) {

  fun present(isCreating: Boolean) {
    view.setupToolbar()
    if (isCreating) navigator.showBackupScreen(data.walletAddress)
  }

  fun sendWalletSaveFileEvent(currentFragment: String) {
    if (currentFragment == BackupCreationFragment::class.java.simpleName) {
      eventSender.sendWalletSaveFileEvent(WalletsAnalytics.ACTION_BACK,
          WalletsAnalytics.STATUS_FAIL, WalletsAnalytics.REASON_CANCELED)
    }
  }
}
