package com.asfoundation.wallet.update_required

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.backup.BackupActivity
import com.appcoins.wallet.ui.arch.data.Navigator
import javax.inject.Inject

class UpdateRequiredNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {

  fun navigateToBackup(walletAddress: String) {
    fragment.startActivity(
      BackupActivity.newIntent(
        context = fragment.requireContext(),
        walletAddress = walletAddress,
        isBackupTrigger = false
      )
    )
  }
}