package com.asfoundation.wallet.main.nav_bar

import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.asf.wallet.NavBarGraphDirections
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.topup.TopUpActivity
import com.asfoundation.wallet.ui.overlay.OverlayFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import javax.inject.Inject

class NavBarFragmentNavigator @Inject constructor(
  private val fragment: Fragment
) : Navigator {

  fun navigateToHome() {
    val bottomNavView: BottomNavigationView =
      fragment.requireActivity().findViewById(R.id.bottom_nav)
    val view: View = bottomNavView.findViewById(R.id.home_graph)
    view.performClick()
  }

  fun navigateToPromotions() {
    val bottomNavView: BottomNavigationView =
      fragment.requireActivity().findViewById(R.id.bottom_nav)
    val view: View = bottomNavView.findViewById(R.id.promotions_graph)
    view.performClick()
  }

  fun navigateToMyWallets() {
    val bottomNavView: BottomNavigationView =
      fragment.requireActivity().findViewById(R.id.bottom_nav)
    val view: View = bottomNavView.findViewById(R.id.my_wallets_graph)
    view.performClick()
  }

  fun navigateToTopUp() {
    val intent = TopUpActivity.newIntent(fragment.requireContext())
      .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK }
    fragment.requireContext().startActivity(intent)
  }

  fun showPromotionsOverlay(activity: MainActivity, index: Int) {
    activity.supportFragmentManager.beginTransaction()
      .setCustomAnimations(
        R.anim.fragment_fade_in_animation,
        R.anim.fragment_fade_out_animation, R.anim.fragment_fade_in_animation,
        R.anim.fragment_fade_out_animation
      )
      .add(
        R.id.tooltip_container,
        OverlayFragment.newInstance(index)
      )
      .addToBackStack(OverlayFragment::class.java.name)
      .commit()
  }

  fun showOnboardingGPInstallScreen(navController: NavController) {
    navigate(navController, NavBarGraphDirections.actionNavigateToGpInstallFragment())
  }
}