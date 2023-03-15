package com.asfoundation.wallet.my_wallets.balance_details

import androidx.navigation.NavController
import com.appcoins.wallet.ui.arch.Navigator
import com.appcoins.wallet.ui.arch.navigate
import javax.inject.Inject

class BalanceDetailsNavigator @Inject constructor(private val navController: NavController) :
  com.appcoins.wallet.ui.arch.Navigator {

  fun navigateToTokenInfo(title: String, image: String, description: String, showTopUp: Boolean) {
    navigate(
      navController,
      BalanceDetailsFragmentDirections.actionNavigateToTokenInfo(
        title,
        image,
        description,
        showTopUp
      )
    )
  }
}