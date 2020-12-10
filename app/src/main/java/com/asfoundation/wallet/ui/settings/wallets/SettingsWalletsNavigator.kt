package com.asfoundation.wallet.ui.settings.wallets

import androidx.fragment.app.FragmentManager

class SettingsWalletsNavigator(private val fragmentManager: FragmentManager) {

  fun hideBottomSheet() = fragmentManager.popBackStack()
}
