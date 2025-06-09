package com.asfoundation.wallet.wallet_reward

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asf.wallet.R
import com.asfoundation.wallet.ui.gamification.GamificationActivity
import com.asfoundation.wallet.ui.settings.entry.SettingsFragment
import javax.inject.Inject

class RewardNavigator
@Inject
constructor(private val fragment: Fragment, private val navController: NavController) : Navigator {

  fun navigateToSettings(mainNavController: NavController, turnOnFingerprint: Boolean = false) {
    val bundle = Bundle()
    bundle.putBoolean(SettingsFragment.TURN_ON_FINGERPRINT, turnOnFingerprint)
    mainNavController.navigate(resId = R.id.action_navigate_to_settings, args = bundle)
  }

  fun showPromoCodeFragment(promoCode: String? = null) {
    navigate(navController, RewardFragmentDirections.actionNavigatePromoCode(promoCode))
  }

  fun showGiftCardFragment(giftCard: String? = null) {
    navigate(navController, RewardFragmentDirections.actionNavigateGiftCard(giftCard))
  }

  fun navigateToGamification(cachedBonus: Double) {
    fragment.startActivity(GamificationActivity.newIntent(fragment.requireContext(), cachedBonus))
  }

}
