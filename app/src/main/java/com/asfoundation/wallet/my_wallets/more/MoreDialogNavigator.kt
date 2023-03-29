package com.asfoundation.wallet.my_wallets.more

import androidx.navigation.NavController
import com.appcoins.wallet.ui.arch.data.Navigator
import com.appcoins.wallet.ui.arch.data.navigate
import javax.inject.Inject

class MoreDialogNavigator @Inject constructor(private val navController: NavController) :
  Navigator {

  fun navigateToCreateNewWallet() {
    navigate(
      navController,
      MoreDialogFragmentDirections.actionNavigateToCreateWallet(needsWalletCreation = true)
    )
  }

  fun navigateToRestoreWallet() {
    navigate(
      navController,
      MoreDialogFragmentDirections.actionNavigateToRecoverWallet(onboardingLayout = false)
    )
  }

  fun navigateToRemoveWallet(
    walletAddress: String,
    totalFiatBalance: String,
    appcoinsBalance: String,
    creditsBalance: String,
    ethereumBalance: String
  ) {
    navigate(
      navController,
      MoreDialogFragmentDirections.actionNavigateToRemoveWallet(
        walletAddress,
        totalFiatBalance,
        appcoinsBalance,
        creditsBalance,
        ethereumBalance
      )
    )
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}