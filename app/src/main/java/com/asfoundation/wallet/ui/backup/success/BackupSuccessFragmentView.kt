package com.asfoundation.wallet.ui.backup.success

import io.reactivex.Observable

interface BackupSuccessFragmentView {

  fun getCloseButtonClick(): Observable<Any>

  fun closeScreen()

  fun setSuccessInfo(info: String)
}
