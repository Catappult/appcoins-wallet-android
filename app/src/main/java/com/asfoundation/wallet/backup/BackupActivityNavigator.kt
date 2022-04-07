package com.asfoundation.wallet.backup

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.asf.wallet.R
import com.asfoundation.wallet.backup.entry.BackupEntryFragment
import javax.inject.Inject

class BackupActivityNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun showBackupScreen(walletAddress: String, isBackupTrigger: Boolean) {
    fragmentManager.commit {
      replace(
        R.id.fragment_container,
        BackupEntryFragment.newInstance(walletAddress, isBackupTrigger)
      )
    }
  }
}
