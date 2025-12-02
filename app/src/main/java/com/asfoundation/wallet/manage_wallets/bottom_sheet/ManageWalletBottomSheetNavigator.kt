package com.asfoundation.wallet.manage_wallets.bottom_sheet

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupWalletEntryFragment
import com.asfoundation.wallet.backup.BackupWalletEntryFragment.Companion.WALLET_NAME
import com.asfoundation.wallet.recover.RecoverActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class ManageWalletBottomSheetNavigator
@Inject
constructor(
  val fragment: Fragment,
  val fragmentManager: FragmentManager,
  private val navController: NavController
) : Navigator {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun navigateToManageNameWallet() {
    val bottomSheet = ManageWalletNameBottomSheetFragment.newInstance()
    bottomSheet.show(fragment.parentFragmentManager, "ManageWalletName")
  }

  fun navigateToRemoveWallet(navController: NavController) {
    navController.navigate(R.id.action_navigate_to_remove_wallet)
  }

  fun navigateToRecoverWallet() {
    val intent =
      RecoverActivity.newIntent(fragment.requireContext(), onboardingLayout = false).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
    openIntent(intent)
  }

  fun navigateToBackup(
    walletAddress: String,
    walletName: String
  ) {
    val bundle = Bundle()
    bundle.putString(BackupWalletEntryFragment.WALLET_ADDRESS_KEY, walletAddress)
    bundle.putString(WALLET_NAME, walletName)
    navController.navigate(R.id.action_navigate_to_backup_entry_wallet, args = bundle)
  }

  private fun openIntent(intent: Intent) = fragment.requireContext().startActivity(intent)
}
