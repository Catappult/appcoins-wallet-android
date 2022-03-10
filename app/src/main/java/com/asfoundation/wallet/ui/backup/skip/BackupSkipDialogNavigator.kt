package com.asfoundation.wallet.ui.backup.skip

import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BackupSkipDialogNavigator(val fragment: Fragment) {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun finishBackup() {
    fragment.activity?.finish()
  }
}