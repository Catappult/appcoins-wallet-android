package com.asfoundation.wallet.ui.backup.creation

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.BackupActivityNavigator
import com.asfoundation.wallet.ui.backup.success.BackupSuccessFragment
import javax.inject.Inject

class BackupCreationNavigator @Inject constructor(private val fragmentManager: FragmentManager,
                              private val activityNavigator: BackupActivityNavigator) {

  fun openSystemFileDirectory(fileName: String) =
      activityNavigator.openSystemFileDirectory(fileName)

  fun showSuccessScreen() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupSuccessFragment.newInstance())
        .commit()
  }
}
