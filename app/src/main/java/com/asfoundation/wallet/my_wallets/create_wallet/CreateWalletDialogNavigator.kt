package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.navigation.NavController
import com.appcoins.wallet.ui.arch.Navigator
import javax.inject.Inject

class CreateWalletDialogNavigator @Inject constructor(private val navController: NavController) :
  com.appcoins.wallet.ui.arch.Navigator {

  fun navigateBack() {
    navController.popBackStack()
  }
}