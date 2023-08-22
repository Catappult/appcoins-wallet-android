package com.appcoins.wallet.feature.backup.ui.save_options

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.appcoins.wallet.feature.backup.ui.R

import com.appcoins.wallet.feature.backup.ui.save_on_device.BackupSaveOnDeviceDialogFragment
import com.appcoins.wallet.feature.backup.ui.success.BackupSuccessFragment
import javax.inject.Inject

class BackupSaveOptionsNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun navigateToSaveOnDeviceScreen(walletAddress: String, password: String) {
    val bottomSheet = BackupSaveOnDeviceDialogFragment.newInstance(walletAddress, password)
    bottomSheet.show(fragmentManager, "SaveOnDeviceDialog")
  }

  fun navigateToSuccessScreen(walletAddress: String) {
    fragmentManager.commit {
      replace(R.id.fragment_container, BackupSuccessFragment.newInstance(walletAddress, false))
      addToBackStack("BackupSaveOptionsFragment")
    }
  }
}
