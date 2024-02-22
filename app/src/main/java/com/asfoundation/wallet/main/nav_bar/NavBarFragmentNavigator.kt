package com.asfoundation.wallet.main.nav_bar

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asf.wallet.NavBarGraphDirections
import com.asf.wallet.R
import com.asfoundation.wallet.main.splash.SplashExtenderFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import javax.inject.Inject

class NavBarFragmentNavigator @Inject constructor(private val fragment: Fragment) : Navigator {

  fun navigateToHome() {
    val bottomNavView: BottomNavigationView =
        fragment.requireActivity().findViewById(R.id.bottom_nav)
    val view: View = bottomNavView.findViewById(R.id.home_graph)
    view.performClick()
  }

  fun navigateToRewards() {
    val bottomNavView: BottomNavigationView =
        fragment.requireActivity().findViewById(R.id.bottom_nav)
    val view: View = bottomNavView.findViewById(R.id.reward_graph)
    view.performClick()
  }

  fun showOnboardingGPInstallScreen(navController: NavController) {
    navigate(navController, NavBarGraphDirections.actionNavigateToGpInstallFragment())
  }

  fun showOnboardingPaymentScreen(navController: NavController) {
    navigate(navController, NavBarGraphDirections.actionNavigateToFirstPaymentFragment())
  }

  fun showOnboardingRecoverGuestWallet(navController: NavController, backup: String) {
    navigate(navController, SplashExtenderFragmentDirections.actionNavigateToOnboardingGraph())
  }
}
