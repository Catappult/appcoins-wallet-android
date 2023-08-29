package com.asfoundation.wallet.backup

import android.os.Bundle
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.asf.wallet.R
import javax.inject.Inject

class BackupEntryNavigator @Inject constructor(private val navController: NavController) :
  Navigator {

  fun navigateBack() = navController.popBackStack()

  fun navigateToManageWallet() {
    navController.popBackStack(R.id.manage_wallet_fragment, true)
  }

  fun navigateToHome() {
    clearBackStack()
  }

  fun showWalletChooseScreen(walletModel: WalletsModel) {
    val bundle =
      Bundle().apply { putSerializable(BackupWalletEntryFragment.WALLET_MODEL_KEY, walletModel) }
    navController.navigate(R.id.action_backup_entry_to_choose_wallet, bundle)
  }

  private fun clearBackStack() {
    with(navController) { while (popBackStack()) popBackStack() }
  }
}
