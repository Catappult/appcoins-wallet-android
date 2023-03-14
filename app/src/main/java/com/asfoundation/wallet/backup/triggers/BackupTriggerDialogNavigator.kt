package com.asfoundation.wallet.backup.triggers

import android.content.Intent
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.backup.BackupActivity
import com.asfoundation.wallet.backup.skip.BackupSkipDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import preferences.BackupTriggerPreferencesDataSource
import javax.inject.Inject

class BackupTriggerDialogNavigator @Inject constructor(val fragment: Fragment) {

  fun navigateToBackupActivity(walletAddress: String) {
    (fragment as BottomSheetDialogFragment).dismiss()
    val intent =
      BackupActivity.newIntent(fragment.requireContext(), walletAddress, isBackupTrigger = true)
        .apply {
          flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    fragment.requireContext().startActivity(intent)
  }

  fun navigateToDismiss(
    walletAddress: String,
    triggerSource: BackupTriggerPreferencesDataSource.TriggerSource
  ) {
    (fragment as BottomSheetDialogFragment).dismiss()
    val bottomSheet = BackupSkipDialogFragment.newInstance(walletAddress, triggerSource)
    bottomSheet.isCancelable = false
    bottomSheet.show(fragment.parentFragmentManager, "BackupDismiss")
  }
}