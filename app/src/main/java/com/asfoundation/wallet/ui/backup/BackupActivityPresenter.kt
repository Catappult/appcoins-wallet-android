package com.asfoundation.wallet.ui.backup

class BackupActivityPresenter(private val view: BackupActivityView) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      view.showBackupScreen()
    }
  }
}
