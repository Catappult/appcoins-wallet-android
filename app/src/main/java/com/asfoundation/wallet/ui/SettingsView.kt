package com.asfoundation.wallet.ui


interface SettingsView {

  fun setupPreferences()
  fun setVerifiedWalletPreference()
  fun setUnverifiedWalletPreference()
  fun setWalletValidationNoNetwork()
  fun setRedeemCodePreference(walletAddress: String)
  fun showError()
}