package com.asfoundation.wallet.backup.entryBottomSheet

import io.reactivex.Observable

interface BackupEntryChooseWalletView {

  fun showBottomSheet()

  fun outsideOfBottomSheetClick(): Observable<Any>
}
