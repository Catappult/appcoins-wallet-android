package com.asfoundation.wallet.onboarding

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import javax.inject.Inject

class OnboardingNavigator @Inject constructor(private val fragment: Fragment) : Navigator {

  fun navigateToTermsBottomSheet() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateTermsConditions()
    )
  }

  fun navigateToNavBar(){
    navigate(fragment.findNavController(), OnboardingFragmentDirections.actionNavigateToNavBarFragment())
  }

  fun navigateToRecover() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToRecoverWalletGraph(onboardingLayout = true)
    )
  }

  fun navigateToCreateWalletDialog() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateCreateWalletDialog(
        needsWalletCreation = true
      )
    )
  }
}