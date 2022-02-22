package com.asfoundation.wallet.ui.backup.entry

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.creation.BackupCreationFragment
import javax.inject.Inject

class BackupWalletNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun showBackupCreationScreen(walletAddress: String, password: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            BackupCreationFragment.newInstance(walletAddress, password))
        .commit()
  }
}
