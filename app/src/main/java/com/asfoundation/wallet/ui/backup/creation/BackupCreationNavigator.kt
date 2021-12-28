package com.asfoundation.wallet.ui.backup.creation

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.BackupActivityNavigator
import com.asfoundation.wallet.ui.backup.save.SaveBackupBottomSheetFragment
import com.asfoundation.wallet.ui.backup.success.BackupSuccessFragment

class BackupCreationNavigator(private val fragmentManager: FragmentManager,
                              private val activityNavigator: BackupActivityNavigator) {

  fun openSystemFileDirectory(fileName: String) =
      activityNavigator.openSystemFileDirectory(fileName) // TODO: REMOVE

  fun showSuccessScreen() {  // TODO: REMOVE
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupSuccessFragment.newInstance())
        .commit()
  }

  fun showSaveOnDeviceScreen(walletAddress: String, password: String) {
    val bottomSheet = SaveBackupBottomSheetFragment.newInstance(walletAddress, password)
    bottomSheet.show(fragmentManager, "SaveBackupBottomSheet")
  }
}
