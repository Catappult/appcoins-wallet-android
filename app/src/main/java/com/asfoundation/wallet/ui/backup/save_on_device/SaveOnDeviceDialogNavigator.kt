package com.asfoundation.wallet.ui.backup.save_on_device

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.success.BackupSuccessFragment

class SaveOnDeviceDialogNavigator(val fragment: SaveOnDeviceDialogFragment,
                                  private val fragmentManager: FragmentManager) {

  fun navigateBack() {
    fragment.dismiss()
  }

  fun navigateToSuccessScreen() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupSuccessFragment.newInstance(false))
        .addToBackStack("SaveBackupDialogFragment")
        .commit()
    fragment.dismiss()
  }
}