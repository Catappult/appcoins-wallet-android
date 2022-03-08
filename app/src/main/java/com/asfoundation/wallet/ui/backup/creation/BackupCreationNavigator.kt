package com.asfoundation.wallet.ui.backup.creation

import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.save_on_device.SaveOnDeviceDialogFragment
import com.asfoundation.wallet.ui.backup.success.BackupSuccessFragment
import javax.inject.Inject

class BackupCreationNavigator @Inject constructor(private val fragment: Fragment) {

  fun navigateToSaveOnDeviceScreen(walletAddress: String, password: String) {
    val bottomSheet = SaveOnDeviceDialogFragment.newInstance(walletAddress, password)
    bottomSheet.show(fragment.requireFragmentManager(), "SaveOnDeviceDialog")
  }

  fun navigateToSuccessScreen() {
    fragment.requireFragmentManager().beginTransaction()
        .replace(R.id.fragment_container, BackupSuccessFragment.newInstance(true))
        .addToBackStack("BackupCreationFragment")
        .commit()
  }
}
