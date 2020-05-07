package com.asfoundation.wallet.ui.backup

import android.net.Uri
import io.reactivex.Observable

interface BackupCreationView {

  fun shareFile(uri: Uri)

  fun getPositiveButtonClick(): Observable<Any>

  fun getNegativeButtonClick(): Observable<Any>

  fun showConfirmation()

  fun enableSaveButton()

  fun showError()

}
