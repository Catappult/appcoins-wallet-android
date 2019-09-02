package com.asfoundation.wallet.ui

import android.content.Context


interface SettingsView {

  fun setupPreferences()

  fun setVerifiedWalletPreference()

  fun setUnverifiedWalletPreference()

  fun setWalletsPreference(walletAddress: String)

  fun setRedeemCodePreference(walletAddress: String)

  fun getContext(): Context?
}