package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.appcoins.wallet.feature.backup.ui.BackupActivity
import javax.inject.Inject

class SettingsWalletsBottomSheetNavigator @Inject constructor(
  val fragmentManager: FragmentManager,
  val fragment: Fragment
) {

  fun navigateToBackup(walletAddress: String) {
    fragment.startActivity(
      com.appcoins.wallet.feature.backup.ui.BackupActivity.newIntent(
        fragment.requireContext(),
        walletAddress,
        isBackupTrigger = false
      )
    )
    fragmentManager.popBackStack()
  }
}
