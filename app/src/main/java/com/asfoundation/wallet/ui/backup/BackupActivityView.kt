package com.asfoundation.wallet.ui.backup

import io.reactivex.Observable

interface BackupActivityView {

  fun askForWritePermissions()

  fun closeScreen()

  fun onPermissionGiven(): Observable<Unit>

  fun onSystemFileIntentResult(): Observable<SystemFileIntentResult>

  fun getCurrentFragment(): String

  fun setupToolbar()

  fun onDocumentFile(systemFileIntentResult: SystemFileIntentResult)
}
