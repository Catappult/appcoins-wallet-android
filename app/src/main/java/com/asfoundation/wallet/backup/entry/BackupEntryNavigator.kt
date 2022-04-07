package com.asfoundation.wallet.backup.entry

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.backup.save_options.BackupSaveOptionsFragment
import com.asfoundation.wallet.backup.skip.BackupSkipDialogFragment
import javax.inject.Inject

class BackupEntryNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun showBackupCreationScreen(walletAddress: String, password: String) {
    fragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        BackupSaveOptionsFragment.newInstance(walletAddress, password)
      )
      .addToBackStack("BackupEntryFragment")
      .commit()
  }

  fun navigateToSkipScreen(walletAddress: String) {
    val bottomSheet = BackupSkipDialogFragment.newInstance(walletAddress = walletAddress)
    bottomSheet.show(fragmentManager, "SkipDialog")
  }
}
