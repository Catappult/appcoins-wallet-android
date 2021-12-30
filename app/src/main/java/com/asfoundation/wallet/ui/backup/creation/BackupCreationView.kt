package com.asfoundation.wallet.ui.backup.creation

import io.reactivex.Observable

interface BackupCreationView {

  fun getSaveOnDeviceButton(): Observable<Any>

  fun getSendToEmailClick(): Observable<String>

  fun showError()

  fun dismiss()

  fun askForWritePermissions()

  fun onPermissionGiven(): Observable<Unit>
}
