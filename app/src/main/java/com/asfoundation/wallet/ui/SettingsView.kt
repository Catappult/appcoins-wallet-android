package com.asfoundation.wallet.ui

import android.content.Intent


interface SettingsView {

  fun setupPreferences()

  fun setRedeemCodePreference(walletAddress: String)

  fun showError()

  fun navigateToIntent(intent: Intent)
}