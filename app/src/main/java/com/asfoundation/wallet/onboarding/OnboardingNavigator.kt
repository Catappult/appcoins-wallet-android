package com.asfoundation.wallet.onboarding

import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate

class OnboardingNavigator(private val fragment: OnboardingFragment) : Navigator {

  fun navigateToTermsBottomSheet() {
    navigate(fragment.findNavController(),
        OnboardingFragmentDirections.actionNavigateTermsConditions())
  }

  fun navigateToMainActivity(fromSupportNotification: Boolean) {
    navigate(fragment.findNavController(),
        OnboardingFragmentDirections.actionNavigateToMainActivity(fromSupportNotification))
  }

  fun navigateToRestoreActivity() {
    navigate(fragment.findNavController(),
        OnboardingFragmentDirections.actionNavigateToRestoreWallet())
  }
}