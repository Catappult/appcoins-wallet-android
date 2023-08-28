package com.asfoundation.wallet.onboarding.gp_install

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import javax.inject.Inject

class OnboardingGPInstallNavigator @Inject constructor(
  private val fragment: Fragment,
  private val packageManager: PackageManager,
) : Navigator {

  fun navigateBackToGame(packageName: String) {
    try {
      packageManager.getLaunchIntentForPackage(packageName)?.let {
        fragment.startActivity(
          it
        )
      }
    } catch (e: Throwable) {
      e.printStackTrace()
      fragment.activity?.finishAffinity()
    }
  }

  fun navigateToExploreWallet() {
    fragment.findNavController().popBackStack()
  }
}