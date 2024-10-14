package com.asfoundation.wallet.manage_cards


import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import javax.inject.Inject

class ManageCardsNavigator
@Inject
constructor(private val fragment: Fragment, private val navController: NavController) : Navigator {

  fun navigateToAddCard() {
    navigate(
      fragment.findNavController(),
      ManageCardsFragmentDirections.actionNavigateToManageAdyenPayment()
    )
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}
