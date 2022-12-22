package com.asfoundation.wallet.onboarding.pending_payment

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import javax.inject.Inject

class OnboardingPaymentNavigator @Inject constructor() : Navigator {
  fun showPaymentMethods(navController: NavController) {
    navigate(
      navController,
      OnboardingPaymentFragmentDirections.actionNavigateToInnerPaymentMethods()
    )
  }
}