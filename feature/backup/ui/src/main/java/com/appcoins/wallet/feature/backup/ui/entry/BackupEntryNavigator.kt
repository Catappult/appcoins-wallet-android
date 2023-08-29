package com.appcoins.wallet.feature.backup.ui.entry

import androidx.fragment.app.FragmentManager
import com.appcoins.wallet.feature.backup.ui.R
import com.appcoins.wallet.feature.backup.ui.save_options.BackupSaveOptionsFragment
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
}
