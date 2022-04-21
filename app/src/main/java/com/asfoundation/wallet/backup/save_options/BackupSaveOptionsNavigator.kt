package com.asfoundation.wallet.backup.save_options

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.asf.wallet.R
import com.asfoundation.wallet.backup.save_on_device.BackupSaveOnDeviceDialogFragment
import com.asfoundation.wallet.backup.success.BackupSuccessFragment
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
