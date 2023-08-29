package com.asfoundation.wallet.wallet.home.bottom_sheet

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.appcoins.wallet.feature.backup.ui.BackupActivity
import com.asf.wallet.R
import com.asfoundation.wallet.recover.RecoverActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class HomeManageWalletBottomSheetNavigator @Inject constructor(
  val fragment: Fragment,
  val fragmentManager: FragmentManager
) {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun navigateToBackup(walletAddress: String) {
    val intent =
      BackupActivity.newIntent(fragment.requireContext(), walletAddress, isBackupTrigger = false)
        .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
    openIntent(intent)
  }

  fun navigateToRecoverWallet() {
    val intent = RecoverActivity.newIntent(fragment.requireContext(), onboardingLayout = false)
      .apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
    openIntent(intent)
  }

  fun navigateToManageWallet(
    mainNavController: NavController
  ) {
    mainNavController.navigate(R.id.action_navigate_to_manage_wallet)
  }

  private fun openIntent(intent: Intent) = fragment.requireContext()
    .startActivity(intent)

}