package com.asfoundation.wallet.manage_cards


import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate

import javax.inject.Inject

class ManageAdyenPaymentNavigator
@Inject
constructor(private val navController: NavController) : Navigator {

  fun navigateToAddCard(navController: NavController) {
    navigate(navController, ManageCardsFragmentDirections.actionNavigateToManageAdyenPayment())
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}
