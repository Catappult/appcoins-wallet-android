package com.asfoundation.wallet.ui.settings.wallets

import io.reactivex.Observable

interface SettingsWalletsView {

  fun showBottomSheet()

  fun outsideOfBottomSheetClick(): Observable<Any>
}
