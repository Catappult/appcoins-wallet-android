package com.asfoundation.wallet.onboarding

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import javax.inject.Inject

class OnboardingNavigator @Inject constructor(private val fragment: Fragment) : Navigator {

  fun navigateToTermsBottomSheet() {
    navigate(fragment.findNavController(),
        OnboardingFragmentDirections.actionNavigateTermsConditions())
  }

  fun navigateToMainActivity(fromSupportNotification: Boolean) {
    navigate(fragment.findNavController(),
        OnboardingFragmentDirections.actionNavigateToMainActivity(fromSupportNotification))
  }

  fun navigateToRecoverActivity() {
    navigate(fragment.findNavController(),
        OnboardingFragmentDirections.actionNavigateToRecoverWalletActivity(onboardingLayout = true))
  }
}