package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.wallets.WalletsModel


interface SettingsView {

  fun setupPreferences()
  fun setVerifiedWalletPreference()
  fun setUnverifiedWalletPreference()
  fun setWalletValidationNoNetwork()
  fun setRedeemCodePreference(walletAddress: String)
  fun showError()
  fun navigateToBackUp(walletAddress: String)
  fun showWalletsBottomSheet(walletModel: WalletsModel)
}