package com.asfoundation.wallet.onboarding

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigator
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

  //when navigation component doesn't have this limitation anymore, this extras should be removed
  fun navigateToMainActivity(fromSupportNotification: Boolean) {
    val clearBackStackExtras = ActivityNavigator.Extras.Builder()
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .build()

    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToMainActivity(fromSupportNotification),
      extras = clearBackStackExtras
    )
  }

  fun navigateToRecoverActivity() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToRecoverWalletActivity(onboardingLayout = true)
    )
  }
}