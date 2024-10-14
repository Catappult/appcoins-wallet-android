package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import javax.inject.Inject

class CreateWalletDialogNavigator @Inject constructor(private val navController: NavController) :
  Navigator {

  fun navigateBack() {
    navController.popBackStack()
  }
}