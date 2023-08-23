package com.asfoundation.wallet.main

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asfoundation.wallet.main.splash.SplashExtenderFragmentDirections
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import javax.inject.Inject

class MainActivityNavigator @Inject constructor() :
  Navigator {

  fun navigateToOnboarding(navController: NavController) {
    navigate(navController, SplashExtenderFragmentDirections.actionNavigateToOnboardingGraph())
  }

  fun showAuthenticationActivity(
    context: Context,
    authenticationResultLauncher: ActivityResultLauncher<Intent>
  ) {
    val intent = AuthenticationPromptActivity.newIntent(context)
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    authenticationResultLauncher.launch(intent)
  }

  fun navigateToAutoUpdate(navController: NavController) {
    navigate(navController, SplashExtenderFragmentDirections.actionNavigateToUpdateRequiredGraph())
  }

  fun navigateToNavBarFragment(navController: NavController) {
    navigate(navController, SplashExtenderFragmentDirections.actionNavigateToNavBarGraph())
  }
}