package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import javax.inject.Inject

class CreateWalletDialogNavigator @Inject constructor(private val navController: NavController) :
  Navigator {

  fun navigateBack() {
    navController.popBackStack()
  }
}