package com.asfoundation.wallet.ui

import io.reactivex.Observable

interface SettingsWalletsView {

  fun showBottomSheet()

  fun navigateToBackup(address: String)

  fun outsideOfBottomSheetClick(): Observable<Any>
}
