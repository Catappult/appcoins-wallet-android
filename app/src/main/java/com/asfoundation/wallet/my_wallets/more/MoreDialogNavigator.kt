package com.asfoundation.wallet.my_wallets.more

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate

class MoreDialogNavigator(private val navController: NavController) : Navigator {

  fun navigateToCreateNewWallet() {
    navigate(navController, MoreDialogFragmentDirections.actionNavigateToCreateWallet())
  }

  fun navigateToRestoreWallet() {
    navigate(navController, MoreDialogFragmentDirections.actionNavigateToRestoreWallet())
  }

  fun navigateToBackupWallet(walletAddress: String) {
    navigate(navController,
        MoreDialogFragmentDirections.actionNavigateToBackupWallet(walletAddress))
  }

  fun navigateToRemoveWallet(walletAddress: String, totalFiatBalance: String,
                             appcoinsBalance: String, creditsBalance: String,
                             ethereumBalance: String) {
    navigate(navController,
        MoreDialogFragmentDirections.actionNavigateToRemoveWallet(walletAddress, totalFiatBalance,
            appcoinsBalance, creditsBalance, ethereumBalance))
  }

}