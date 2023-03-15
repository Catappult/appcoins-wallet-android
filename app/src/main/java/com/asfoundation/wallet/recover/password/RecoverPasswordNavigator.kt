package com.asfoundation.wallet.recover.password

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.ui.arch.Navigator
import com.appcoins.wallet.ui.arch.navigate
import com.asfoundation.wallet.recover.RecoverActivity
import javax.inject.Inject

class RecoverPasswordNavigator @Inject constructor(val fragment: Fragment) :
  com.appcoins.wallet.ui.arch.Navigator {

  fun navigateToCreateWalletDialog(isFromOnboarding: Boolean) {
    navigate(
      fragment.findNavController(),
      RecoverPasswordFragmentDirections.actionNavigateCreateWalletDialog(
        needsWalletCreation = false,
        isFromOnboarding = isFromOnboarding
      )
    )
  }

  fun navigateBack() {
    fragment.findNavController().popBackStack()
  }

  fun navigateToNavigationBar() {
    /* Temporary workaround for the RecoverActivity */
    fragment.requireActivity()
      .takeIf { it is RecoverActivity }?.finish()
      ?: navigate(
        fragment.findNavController(),
        RecoverPasswordFragmentDirections.actionNavigateToNavBarFragment()
      )
  }
}