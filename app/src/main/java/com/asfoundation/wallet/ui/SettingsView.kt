package com.asfoundation.wallet.ui

import android.content.Intent


interface SettingsView {

  fun setupPreferences()

  fun setVerifiedWalletPreference()

  fun setUnverifiedWalletPreference()

  fun setWalletValidationNoNetwork()

  fun setRedeemCodePreference(walletAddress: String)

  fun showError()

  fun navigateToIntent(intent: Intent)
}