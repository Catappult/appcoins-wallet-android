package com.appcoins.wallet.feature.backup.ui

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryFragment
import javax.inject.Inject

class BackupActivityNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun showBackupScreen(walletAddress: String) {
    fragmentManager.commit {
      replace(
        R.id.fragment_container,
        BackupEntryFragment.newInstance(walletAddress)
      )
    }
  }
}
