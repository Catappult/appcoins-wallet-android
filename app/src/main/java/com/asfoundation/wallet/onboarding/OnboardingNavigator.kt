package com.asfoundation.wallet.onboarding

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asf.wallet.R
import com.appcoins.wallet.ui.arch.data.Navigator
import com.appcoins.wallet.ui.arch.data.navigate
import javax.inject.Inject

class OnboardingNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {

  fun navigateToNavBar() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToNavBarFragment()
    )
  }

  fun navigateToRecover() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToRecoverWalletGraph(onboardingLayout = true)
    )
  }

  fun navigateToCreateWalletDialog() {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateCreateWalletDialog(
        needsWalletCreation = true
      )
    )
  }

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
}