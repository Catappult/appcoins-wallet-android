package com.asfoundation.wallet.recover.password

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asfoundation.wallet.recover.RecoverActivity
import com.asfoundation.wallet.recover.success.RecoveryWalletSuccessBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class RecoverPasswordNavigator @Inject constructor(val fragment: Fragment) :
  Navigator {

  fun navigateToCreateWalletDialog(isFromOnboarding: Boolean) {
    navigate(
      fragment.findNavController(),
      RecoverPasswordFragmentDirections.actionNavigateCreateWalletDialog(
        needsWalletCreation = false,
        isFromOnboarding = isFromOnboarding
      )
    )
  }

  fun navigateToSuccess(isFromOnboarding: Boolean) {
    val bottomSheet = RecoveryWalletSuccessBottomSheetFragment.newInstance(isFromOnboarding)
    bottomSheet.show(fragment.parentFragmentManager, "RecoveryWalletSuccess")
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