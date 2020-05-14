package com.asfoundation.wallet.ui.backup

import io.reactivex.Observable

interface BackupActivityView {

  fun showBackupScreen()

  fun showBackupCreationScreen(password: String)

  fun startWalletBackup()

  fun showSuccessScreen()

  fun closeScreen()

  fun onPermissionGiven(): Observable<Unit>

  fun openSystemFileDirectory(fileName: String)

  fun onSystemFileIntentResult(): Observable<SystemFileIntentResult>
}
