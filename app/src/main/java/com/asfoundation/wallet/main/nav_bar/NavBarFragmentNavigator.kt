package com.asfoundation.wallet.main.nav_bar

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asf.wallet.NavBarGraphDirections
import com.asf.wallet.R
import com.asfoundation.wallet.main.splash.SplashExtenderFragmentDirections
import com.asfoundation.wallet.wallet_reward.RewardFragment
import javax.inject.Inject

class NavBarFragmentNavigator @Inject constructor() : Navigator {

  fun navigateToHome(navController: NavController) {
    navController.navigate(
      resId = R.id.home_graph,
      args = null,
      navOptions = NavOptions.Builder().apply { setPopUpTo(R.id.home_graph, false) }.build()
    )
  }

  fun navigateToRewards(navController: NavController, giftCard: String? = null) {
    navController.navigate(
      resId = R.id.reward_graph,
      args = giftCard?.let { Bundle().apply { putString(RewardFragment.EXTRA_GIFT_CARD, giftCard) } },
      navOptions = NavOptions.Builder().apply { setPopUpTo(R.id.reward_fragment, true) }.build()
    )
  }

  fun showOnboardingGPInstallScreen(navController: NavController) {
    navigate(navController, NavBarGraphDirections.actionNavigateToGpInstallFragment())
  }

  fun showOnboardingPaymentScreen(navController: NavController) {
    navigate(navController, NavBarGraphDirections.actionNavigateToFirstPaymentFragment())
  }

  fun showOnboardingRecoverGuestWallet(navController: NavController) {
    navigate(navController, SplashExtenderFragmentDirections.actionNavigateToOnboardingGraph())
  }
}
