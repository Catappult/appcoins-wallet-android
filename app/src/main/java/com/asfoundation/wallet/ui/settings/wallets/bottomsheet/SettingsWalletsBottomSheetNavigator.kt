package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.ui.backup.BackupActivity

class SettingsWalletsBottomSheetNavigator(val fragmentManager: FragmentManager,
                                          val fragment: Fragment) {

  fun navigateToBackup(walletAddress: String) {
    fragment.startActivity(BackupActivity.newIntent(fragment.requireContext(), walletAddress))
    fragmentManager.popBackStack()
  }
}
