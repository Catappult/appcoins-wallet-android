package com.asfoundation.wallet.ui.backup

import io.reactivex.Observable

interface BackupCreationView {

  fun shareFile(uri: String)

  fun getPositiveButtonClick(): Observable<Any>

  fun getNegativeButtonClick(): Observable<Any>

  fun showConfirmation()

}
