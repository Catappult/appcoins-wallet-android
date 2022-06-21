package com.asfoundation.wallet.my_wallets.balance_details

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import javax.inject.Inject

class BalanceDetailsNavigator @Inject constructor(private val navController: NavController) :
  Navigator {

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