package com.asfoundation.wallet.my_wallets.change

import androidx.navigation.NavController

class ChangeActiveWalletDialogNavigator(private val navController: NavController) {

  fun navigateBack() {
    navController.popBackStack()
  }
}