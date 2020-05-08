package com.asfoundation.wallet.ui.backup

import android.net.Uri
import io.reactivex.Observable

interface BackupCreationView {

  fun shareFile(uri: Uri)

  fun getPositiveButtonClick(): Observable<Any>

  fun getSaveAgainClick(): Observable<Any>

  fun showConfirmation()

  fun enableSaveButton()

  fun showError()

  fun showSaveOnDeviceDialog(defaultName: String, path: String)

  fun getDialogCancelClick(): Observable<Any>

  fun getDialogSaveClick(): Observable<String>

  fun closeDialog()
}
