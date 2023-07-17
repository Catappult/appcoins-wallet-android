package com.asfoundation.wallet.manage_wallets.bottom_sheet

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.appcoins.wallet.feature.backup.ui.BackupActivity
import com.asf.wallet.R
import com.asfoundation.wallet.my_wallets.more.MoreDialogFragmentDirections
import com.asfoundation.wallet.recover.RecoverActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class ManageWalletBottomSheetNavigator @Inject constructor(
  val fragment: Fragment,
  val fragmentManager: FragmentManager,
  private val navController: NavController
): Navigator {

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
    val intent = RecoverActivity.newIntent(fragment.requireContext(), onboardingLayout = false)
      .apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
    openIntent(intent)
  }

  private fun openIntent(intent: Intent) = fragment.requireContext()
    .startActivity(intent)

}