package com.asfoundation.wallet.recover.password

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.recover.RecoverActivity
import javax.inject.Inject

class RecoverPasswordNavigator @Inject constructor(val fragment: Fragment) : Navigator {

  fun navigateToCreateWalletDialog() {
    navigate(
      fragment.findNavController(),
      RecoverPasswordFragmentDirections.actionNavigateCreateWalletDialog(needsWalletCreation = false)
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