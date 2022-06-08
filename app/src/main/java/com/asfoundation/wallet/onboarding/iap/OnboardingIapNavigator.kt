package com.asfoundation.wallet.onboarding.iap

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.onboarding.OnboardingFragmentDirections
import javax.inject.Inject

class OnboardingIapNavigator @Inject constructor(private val fragment: Fragment) : Navigator {

  fun navigateToCreateWalletDialog() {
    navigate(
      fragment.findNavController(),
      OnboardingIapFragmentDirections.actionNavigateCreateWalletDialog(needsWalletCreation = true)
    )
  }
  fun navigateBackToGame() {
    fragment.requireActivity().finish()
  }

  fun navigateToTermsBottomSheet() {
    navigate(
      fragment.findNavController(),
      OnboardingIapFragmentDirections.actionNavigateTermsConditions()
    )
  }

  fun closeOnboarding(){
    fragment.requireActivity().finish()
  }
}