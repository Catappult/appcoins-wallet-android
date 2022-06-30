package com.asfoundation.wallet.onboarding.iap

import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.onboarding.bottom_sheet.TermsConditionsBottomSheetFragment
import com.asfoundation.wallet.onboarding.use_cases.GetOnboardingFromIapPackageNameUseCase
import javax.inject.Inject


class OnboardingIapNavigator @Inject constructor(
  private val fragment: Fragment,
  private val packageManager: PackageManager,
  private val onboardingFromIapPackageNameUseCase: GetOnboardingFromIapPackageNameUseCase
) : Navigator {

  fun navigateBackToGame() {
    val launchIntent: Intent? = onboardingFromIapPackageNameUseCase()?.let {
      packageManager.getLaunchIntentForPackage(it)
    }
    fragment.startActivity(launchIntent)
  }

  fun navigateToTermsConditionsBottomSheet() {
    TermsConditionsBottomSheetFragment.newInstance()
      .show(fragment.parentFragmentManager, "TermsConditionsBottomSheetFragment")
  }

  fun closeOnboarding() {
    fragment.parentFragmentManager.popBackStack()
  }
}