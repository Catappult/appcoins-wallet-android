package com.asfoundation.wallet.recover.password

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.onboarding.OnboardingFragmentDirections
import javax.inject.Inject

class RecoverPasswordNavigator @Inject constructor(val fragment: Fragment) : Navigator {

  fun navigateToCreateWalletDialog() {
    navigate(
      fragment.findNavController(),
      RecoverPasswordFragmentDirections.actionNavigateCreateWalletDialog()
    )
  }

  fun navigateBack() {
    fragment.findNavController().popBackStack()
  }

  fun navigateToMainActivity(fromSupportNotification: Boolean) {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToMainActivity(fromSupportNotification)
    )
  }
}