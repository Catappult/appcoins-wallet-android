package com.asfoundation.wallet.ui.backup

class BackupActivityPresenter(private val view: BackupActivityView) {

  var currentFragmentName: String = BackupWalletFragment::class.java.simpleName;

  fun present(isCreating: Boolean) {
    if (isCreating) view.showBackupScreen()
  }
}
