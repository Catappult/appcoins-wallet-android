package com.asfoundation.wallet.onboarding.iap

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogFragment
import com.asfoundation.wallet.onboarding.bottom_sheet.TermsConditionsBottomSheetFragment
import javax.inject.Inject

class OnboardingIapNavigator @Inject constructor(private val fragment: Fragment) : Navigator {

  fun navigateToCreateWalletDialog() {
    CreateWalletDialogFragment.newInstance(needsWalletCreation = true)
      .show(fragment.parentFragmentManager, "CreateWalletDialogFragment")
  }
  fun navigateBackToGame() {
    fragment.requireActivity().finish()
  }

  fun navigateToTermsConditionsBottomSheet() {
    TermsConditionsBottomSheetFragment.newInstance()
      .show(fragment.parentFragmentManager, "TermsConditionsBottomSheetFragment")
  }

  fun closeOnboarding(){
    fragment.parentFragmentManager.popBackStack()
  }
}