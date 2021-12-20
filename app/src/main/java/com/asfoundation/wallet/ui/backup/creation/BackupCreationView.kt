package com.asfoundation.wallet.ui.backup.creation

import android.net.Uri
import com.asfoundation.wallet.ui.backup.SystemFileIntentResult
import io.reactivex.Observable

interface BackupCreationView {

  fun shareFile(uri: Uri)

  fun getFirstSaveClick(): Observable<Any>

  fun getSaveOnDeviceButton(): Observable<Any>

  fun getFinishClick(): Observable<Any>

  fun showConfirmation()

  fun enableSaveButton()

  fun showError()

  fun showSaveOnDeviceDialog(defaultName: String, path: String?)

  fun getDialogCancelClick(): Observable<Any>

  fun getDialogSaveClick(): Observable<String>

  fun closeDialog()

  fun onSystemFileIntentResult(): Observable<SystemFileIntentResult>

  fun closeScreen()

  fun askForWritePermissions()

  fun onPermissionGiven(): Observable<Unit>
}
