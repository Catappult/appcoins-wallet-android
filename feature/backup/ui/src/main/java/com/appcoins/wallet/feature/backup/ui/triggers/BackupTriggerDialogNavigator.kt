package com.appcoins.wallet.feature.backup.ui.triggers

import android.content.Intent
import androidx.fragment.app.Fragment
import com.appcoins.wallet.feature.backup.ui.BackupActivity
import com.appcoins.wallet.feature.backup.ui.skip.BackupSkipDialogFragment
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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