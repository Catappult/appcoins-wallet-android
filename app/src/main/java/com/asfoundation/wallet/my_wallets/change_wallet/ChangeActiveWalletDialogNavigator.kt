package com.asfoundation.wallet.my_wallets.change_wallet

import androidx.navigation.NavController

class ChangeActiveWalletDialogNavigator(private val navController: NavController) {

  fun navigateBack() {
    navController.popBackStack()
  }
}