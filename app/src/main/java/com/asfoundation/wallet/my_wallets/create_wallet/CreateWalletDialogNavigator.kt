package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.navigation.NavController
import javax.inject.Inject

class CreateWalletDialogNavigator @Inject constructor(private val navController: NavController) {

  fun navigateBack() {
    navController.popBackStack()
  }
}