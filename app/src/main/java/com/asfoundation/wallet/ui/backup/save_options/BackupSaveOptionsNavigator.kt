package com.asfoundation.wallet.ui.backup.save_options

import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.save_on_device.BackupSaveOnDeviceDialogFragment
import com.asfoundation.wallet.ui.backup.success.BackupSuccessFragment
import javax.inject.Inject

class BackupSaveOptionsNavigator @Inject constructor(private val fragment: Fragment) {

  fun navigateToSaveOnDeviceScreen(walletAddress: String, password: String) {
    val bottomSheet = BackupSaveOnDeviceDialogFragment.newInstance(walletAddress, password)
    bottomSheet.show(fragment.requireFragmentManager(), "SaveOnDeviceDialog")
  }

  fun navigateToSuccessScreen() {
    fragment.requireFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, BackupSuccessFragment.newInstance(true))
      .addToBackStack("BackupSaveOptionsFragment")
        .commit()
  }
}
