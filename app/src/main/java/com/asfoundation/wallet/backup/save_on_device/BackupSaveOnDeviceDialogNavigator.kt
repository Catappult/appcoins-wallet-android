package com.asfoundation.wallet.backup.save_on_device

import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.backup.success.BackupSuccessFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class BackupSaveOnDeviceDialogNavigator @Inject constructor(val fragment: Fragment) {

  fun dismiss() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun navigateToSuccessScreen() {
    fragment.requireFragmentManager().beginTransaction()
        .replace(R.id.fragment_container, BackupSuccessFragment.newInstance(false))
        .addToBackStack("SaveBackupDialogFragment")
        .commit()
    (fragment as BottomSheetDialogFragment).dismiss()
  }
}