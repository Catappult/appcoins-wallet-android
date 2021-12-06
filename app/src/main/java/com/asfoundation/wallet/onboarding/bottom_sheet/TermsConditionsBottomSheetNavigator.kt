package com.asfoundation.wallet.onboarding.bottom_sheet

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.onboarding.OnboardingFragmentDirections

class TermsConditionsBottomSheetNavigator(
    private val fragment: TermsConditionsBottomSheetFragment) : Navigator {

  fun navigateToBrowser(uri: Uri) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      fragment.requireContext()
          .startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(fragment.requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
          .show()
    }
  }

  fun navigateBack() {
    fragment.findNavController().popBackStack()
  }
  
  fun navigateToCreateWalletDialog() {
    Log.d("APPC-2781", "TermsConditionsBottomSheetNavigator: navigateToCreateWalletDialog: ")
    navigate(fragment.findNavController(),
        TermsConditionsBottomSheetFragmentDirections.actionNavigateCreateWalletDialog())
  }
}