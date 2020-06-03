package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.wallets.WalletsModel

interface SettingsActivityView {
  fun showWalletsBottomSheet(walletModel: WalletsModel)
  fun navigateToBackup(address: String, popBackStack: Boolean = false)
}
