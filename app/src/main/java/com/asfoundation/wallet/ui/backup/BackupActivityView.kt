package com.asfoundation.wallet.ui.backup

interface BackupActivityView {

  fun showBackupScreen()

  fun showBackupCreationScreen(password: String)

  fun startWalletBackup()

  fun showSuccessScreen()

  fun closeScreen()

}
