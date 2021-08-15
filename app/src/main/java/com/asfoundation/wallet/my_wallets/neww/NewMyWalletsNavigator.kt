package com.asfoundation.wallet.my_wallets.neww

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.ui.wallets.WalletBalance

class NewMyWalletsNavigator(private val navController: NavController) : Navigator {

  fun navigateToChangeActiveWallet(walletBalance: WalletBalance) {
    navigate(navController,
        NewMyWalletsFragmentDirections.actionNavigateToChangeActiveWallet(walletBalance))
  }

  fun navigateToCreateNewWallet() {
    navigate(navController, NewMyWalletsFragmentDirections.actionNavigateToCreateWallet())
  }
}