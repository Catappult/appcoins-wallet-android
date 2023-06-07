package com.asfoundation.wallet.update_required

import androidx.fragment.app.Fragment
import com.appcoins.wallet.feature.backup.ui.BackupActivity
import com.appcoins.wallet.core.arch.data.Navigator
import javax.inject.Inject

class UpdateRequiredNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {

  fun navigateToBackup(walletAddress: String) {
    fragment.startActivity(
      com.appcoins.wallet.feature.backup.ui.BackupActivity.newIntent(
        context = fragment.requireContext(),
        walletAddress = walletAddress,
        isBackupTrigger = false
      )
    )
  }
}