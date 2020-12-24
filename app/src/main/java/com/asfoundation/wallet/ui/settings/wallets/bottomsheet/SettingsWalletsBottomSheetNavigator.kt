package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.ui.backup.BackupActivity

class SettingsWalletsBottomSheetNavigator(val fragmentManager: FragmentManager,
                                          val fragment: SettingsWalletsBottomSheetFragment) {

  fun navigateToBackup(walletAddress: String) {
    fragment.startActivity(BackupActivity.newIntent(fragment.context!!, walletAddress))
    fragmentManager.popBackStack()
  }
}
