package com.asfoundation.wallet.ui.backup

import io.reactivex.Observable

interface BackupSuccessFragmentView {
  fun getCloseButtonClick(): Observable<Any>
}
