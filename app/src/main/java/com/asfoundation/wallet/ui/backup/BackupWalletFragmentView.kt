package com.asfoundation.wallet.ui.backup

import io.reactivex.Observable

interface BackupWalletFragmentView {

  fun getBackupButton(): Observable<String>
}
