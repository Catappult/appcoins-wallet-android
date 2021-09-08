package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.navigation.NavController

class CreateWalletDialogNavigator(private val navController: NavController) {

  fun navigateBack() {
    navController.popBackStack()
  }
}