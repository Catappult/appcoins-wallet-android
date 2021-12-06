package com.asfoundation.wallet.onboarding

import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate

class OnboardingNavigator(private val fragment: OnboardingFragment) : Navigator {

//  fun navigateToRestore() {
//    fragment.requireContext()
//        .startActivity(RestoreWalletActivity.newIntent(fragment.requireContext()))
//  }
//
//  fun openBottomSheet() {
//    TermsConditionsBottomSheetFragment.newInstance()
//        .show(fragment.parentFragmentManager, "TermsConditionsBottomSheet")
//  }
//
//    fun navigateToMainActivity(fromSupportNotification: Boolean) {
//    val intent = MainActivity.newIntent(fragment.requireContext(), fromSupportNotification)
//    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//    fragment.requireContext()
//        .startActivity(intent)
//  }

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