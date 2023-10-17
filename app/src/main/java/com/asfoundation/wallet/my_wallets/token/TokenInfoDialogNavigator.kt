package com.asfoundation.wallet.my_wallets.token

import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import javax.inject.Inject

class TokenInfoDialogNavigator @Inject constructor(private val navController: NavController) :
  Navigator {

  fun navigateToTopUp() {
    navigate(navController, TokenInfoDialogFragmentDirections.actionNavigateToTopUp())
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}