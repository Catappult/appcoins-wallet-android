package com.asfoundation.wallet.recover.password

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigator
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
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

  //when navigation component doesn't have this limitation anymore, this extras should be removed and this should work with popUpTo
  fun navigateToMainActivity(fromSupportNotification: Boolean) {
    val clearBackStackExtras = ActivityNavigator.Extras.Builder()
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .build()

    navigate(
      fragment.findNavController(),
      RecoverPasswordFragmentDirections.actionNavigateToMainActivity(fromSupportNotification),
      extras = clearBackStackExtras
    )
  }
}