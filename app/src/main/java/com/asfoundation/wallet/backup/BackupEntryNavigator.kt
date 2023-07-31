package com.asfoundation.wallet.backup

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.asf.wallet.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class BackupEntryNavigator @Inject constructor(
  private val fragment: Fragment,
  private val navController: NavController
) : Navigator {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun handleBackPress() {
    navController.popBackStack()
  }

  fun navigateToManageWallet(
    mainNavController: NavController
  ) {
    mainNavController.navigate(R.id.action_navigate_to_manage_wallet)
  }

  fun navigateToHome(  mainNavController: NavController
  ) {
    mainNavController.navigate(R.id.action_success_screen_back_to_home)
  }

  fun showWalletChooseScreen(
    walletModel : WalletsModel,
    mainNavController : NavController
  ) {
    val bundle = Bundle().apply {
      putSerializable(BackupWalletEntryFragment.WALLET_MODEL_KEY, walletModel)
    }
    mainNavController.navigate(R.id.action_backup_entry_to_choose_wallet, bundle)

  }
}