package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.onboarding.OnboardingFragmentDirections
import javax.inject.Inject

class CreateWalletDialogNavigator @Inject constructor(private val navController: NavController) : Navigator{

  fun navigateBack() {
    navController.popBackStack()
  }

  fun navigateToNavBar() {
    navigate(
      navController,
      CreateWalletDialogFragmentDirections.actionNavigateToNavBarFragment()
    )
  }
}