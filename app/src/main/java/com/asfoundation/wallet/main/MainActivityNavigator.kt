package com.asfoundation.wallet.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asf.wallet.R
import com.asfoundation.wallet.main.nav_bar.NavBarFragment
import com.asfoundation.wallet.main.splash.SplashExtenderFragmentDirections
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import javax.inject.Inject

class MainActivityNavigator @Inject constructor() :
  Navigator {

  fun navigateToOnboarding(navController: NavController) {
    navigate(navController, SplashExtenderFragmentDirections.actionNavigateToOnboardingGraph())
  }

  fun navigateToOnboardingRecoverGuestWallet(
    navController: NavController,
    backup: String,
    flow: String
  ) {
    val bundle = Bundle().apply {
      putString("backup", backup)
      putString("flow", flow)
    }
    navController.setGraph(R.navigation.onboarding_graph, bundle)
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

  fun navigateToPayPalVerificationFragment(navController: NavController) {
    navigate(navController, SplashExtenderFragmentDirections.navigateToPaypalVerificationFragment())
  }

  fun navigateToGiftCardFromSplashScreen(navController: NavController, giftCard: String) {
    navigate(
      navController,
      SplashExtenderFragmentDirections.actionNavigateToNavBarGraph(giftCard),
    )
  }

  fun navigateToGiftCard(navController: NavController, giftCard: String) {
    navigate(
      navController = navController,
      resId = R.id.navigate_to_nav_bar_fragment,
      args = Bundle().apply { putString(NavBarFragment.EXTRA_GIFT_CARD, giftCard) },
      navOptions = NavOptions.Builder().setPopUpTo(R.id.nav_bar_fragment, true).build()
    )
  }

  fun navigateToPromoCodeFromSplashScreen(navController: NavController, promoCode: String) {
    navigate(
      navController,
      SplashExtenderFragmentDirections.actionNavigateToNavBarGraph(giftCard = null, promoCode = promoCode),
    )
  }

  fun navigateToPromoCode(navController: NavController, promoCode: String) {
    navigate(
      navController = navController,
      resId = R.id.navigate_to_nav_bar_fragment,
      args = Bundle().apply { putString(NavBarFragment.EXTRA_PROMO_CODE, promoCode) },
      navOptions = NavOptions.Builder().setPopUpTo(R.id.nav_bar_fragment, true).build()
    )
  }
}