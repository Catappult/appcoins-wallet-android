package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.Observable

interface SettingsActivityView {

  fun showWalletsBottomSheet(walletModel: WalletsModel)

  fun navigateToBackup(address: String, popBackStack: Boolean = false)

  fun hideBottomSheet()

  fun showAuthentication()

  fun authenticationResult(): Observable<Boolean>
}
