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

  fun navigateToTokenInfo(title: String, image: String, description: String, showTopUp: Boolean) {
    navigate(navController,
        NewMyWalletsFragmentDirections.actionNavigateToTokenInfo(title, image, description,
            showTopUp))
  }

  fun navigateToMore(walletAddress: String, totalFiatBalance: String,
                     appcoinsBalance: String, creditsBalance: String,
                     ethereumBalance: String, showDeleteWallet: Boolean) {
    navigate(navController,
        NewMyWalletsFragmentDirections.actionNavigateToMore(walletAddress, totalFiatBalance,
            appcoinsBalance, creditsBalance, ethereumBalance, showDeleteWallet))
  }
}