package com.asfoundation.wallet.onboarding.iap

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.onboarding.use_cases.GetOnboardingFromIapPackageNameUseCase
import javax.inject.Inject

class OnboardingIapNavigator @Inject constructor(
  private val fragment: Fragment,
  private val packageManager: PackageManager,
  private val onboardingFromIapPackageNameUseCase: GetOnboardingFromIapPackageNameUseCase
) : Navigator {

  fun navigateBackToGame() {
    try {
      val launchIntent = onboardingFromIapPackageNameUseCase()?.let {
        packageManager.getLaunchIntentForPackage(it)
      }
      fragment.startActivity(launchIntent)
    } catch (e: Throwable) {
      e.printStackTrace()
      fragment.activity?.finishAffinity()
    }
  }

  fun navigateToTermsConditionsBottomSheet() {
    navigate(
      fragment.findNavController(),
      OnboardingIapFragmentDirections.actionNavigateIapTermsConditions()
    )
  }
}