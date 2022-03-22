package com.asfoundation.wallet.my_wallets.change_wallet

import androidx.navigation.NavController
import javax.inject.Inject

class ChangeActiveWalletDialogNavigator @Inject constructor(private val navController: NavController) {

  fun navigateBack() {
    navController.popBackStack()
  }
}