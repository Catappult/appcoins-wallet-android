package com.asfoundation.wallet.my_wallets.name

import androidx.navigation.NavController
import com.appcoins.wallet.ui.arch.Navigator
import javax.inject.Inject

class NameDialogNavigator @Inject constructor(private val navController: NavController) :
  Navigator {

  fun navigateBack() {
    navController.popBackStack()
  }
}