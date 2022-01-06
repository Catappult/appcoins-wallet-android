package com.asfoundation.wallet.ui.backup.entry

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.creation.BackupCreationFragment
import com.asfoundation.wallet.ui.backup.skip.SkipDialogFragment

class BackupWalletNavigator(private val fragmentManager: FragmentManager) {

  fun showBackupCreationScreen(walletAddress: String, password: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            BackupCreationFragment.newInstance(walletAddress, password))
        .addToBackStack("BackupWalletFragment")
        .commit()
  }

  fun navigateToSkipScreen() {
    val bottomSheet = SkipDialogFragment.newInstance()
    bottomSheet.show(fragmentManager, "SkipDialog")
  }
}
