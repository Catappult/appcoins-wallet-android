package com.asfoundation.wallet.onboarding

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.onboarding.bottom_sheet.TermsConditionsBottomSheetFragment
import com.asfoundation.wallet.recover.entry.RecoverEntryFragment
import javax.inject.Inject

class OnboardingNavigator @Inject constructor(private val fragmentManager: FragmentManager) : Navigator {

  fun navigateToTermsBottomSheet() {
    TermsConditionsBottomSheetFragment.newInstance()
      .show(fragmentManager, "TermsConditionsBottomSheetFragment")
  }

  fun closeOnboarding(){
    fragmentManager.popBackStack()
  }

  fun navigateToRecoverActivity() {
    fragmentManager.commit {
      replace(R.id.fragment_container, RecoverEntryFragment.newInstance(onboardingLayout = true))
      addToBackStack("OnboardingFragment")
    }
  }
}