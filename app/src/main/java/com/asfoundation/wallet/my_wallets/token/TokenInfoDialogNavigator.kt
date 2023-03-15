package com.asfoundation.wallet.my_wallets.token

import androidx.navigation.NavController
import com.appcoins.wallet.ui.arch.Navigator
import com.appcoins.wallet.ui.arch.navigate
import javax.inject.Inject

class TokenInfoDialogNavigator @Inject constructor(private val navController: NavController) :
  com.appcoins.wallet.ui.arch.Navigator {

  fun navigateToTopUp() {
    navigate(navController, TokenInfoDialogFragmentDirections.actionNavigateToTopUp())
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}