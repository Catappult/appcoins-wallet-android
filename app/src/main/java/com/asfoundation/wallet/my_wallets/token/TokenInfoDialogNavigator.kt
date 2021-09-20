package com.asfoundation.wallet.my_wallets.token

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate

class TokenInfoDialogNavigator(private val navController: NavController) : Navigator {

  fun navigateToTopUp() {
    navigate(navController, TokenInfoDialogFragmentDirections.actionNavigateToTopUp())
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}