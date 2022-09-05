package com.asfoundation.wallet.onboarding.iap

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import javax.inject.Inject

class OnboardingIapNavigator @Inject constructor(
  private val fragment: Fragment,
  private val packageManager: PackageManager,
) : Navigator {

  fun navigateBackToGame(packageName: String) {
    try {
      fragment.startActivity(
        packageManager.getLaunchIntentForPackage(packageName)
      )
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