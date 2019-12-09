package com.asfoundation.wallet.ui


interface SettingsView {

  fun setupPreferences()
  fun setVerifiedWalletPreference()
  fun setUnverifiedWalletPreference()
  fun setWalletValidationNoNetwork()
  fun setManageWalletPreference(walletAddress: String)
  fun setRedeemCodePreference(walletAddress: String)
}