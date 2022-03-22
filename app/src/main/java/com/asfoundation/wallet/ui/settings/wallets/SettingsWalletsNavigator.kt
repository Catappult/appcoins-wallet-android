package com.asfoundation.wallet.ui.settings.wallets

import androidx.fragment.app.FragmentManager
import javax.inject.Inject

class SettingsWalletsNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun hideBottomSheet() = fragmentManager.popBackStack()
}
