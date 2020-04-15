package com.asfoundation.wallet.ui.backup

import io.reactivex.Observable

interface BackupCreationView {

  fun shareFile(uri: String)

  fun getBackupClick(): Observable<Any>

}
