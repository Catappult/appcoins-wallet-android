package com.asfoundation.wallet.ui.backup.save

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.success.BackupSuccessFragment

class SaveBackupBottomSheetNavigator(val fragment: SaveBackupBottomSheetFragment,
                                     private val fragmentManager: FragmentManager) {

  fun navigateBack() {
    fragment.dismiss()
  }

  fun navigateToSuccessScreen() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupSuccessFragment.newInstance(false))
        .commit()
    fragment.dismiss()
  }
}