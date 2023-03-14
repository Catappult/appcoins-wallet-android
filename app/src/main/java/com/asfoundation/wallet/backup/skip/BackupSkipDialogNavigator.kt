package com.asfoundation.wallet.backup.skip

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.backup.triggers.BackupTriggerDialogFragment
import preferences.BackupTriggerPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class BackupSkipDialogNavigator @Inject constructor(
  val fragment: Fragment,
  val backupTriggerPreferences: BackupTriggerPreferences
) {

  fun navigateBack(walletAddress: String, triggerSource: BackupTriggerPreferences.TriggerSource) {
    (fragment as BottomSheetDialogFragment).dismiss()
    val bottomSheet = BackupTriggerDialogFragment.newInstance(walletAddress, triggerSource)
    bottomSheet.isCancelable = false
    bottomSheet.show(fragment.parentFragmentManager, "BackupTriggerFromSkip")
  }

  fun finishBackup(walletAddress: String) {
    backupTriggerPreferences.setTriggerState(
      walletAddress = walletAddress,
      active = false,
      triggerSource = BackupTriggerPreferences.TriggerSource.DISABLED
    )
    (fragment as BottomSheetDialogFragment).dismiss()
  }
}