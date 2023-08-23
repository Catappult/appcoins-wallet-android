package com.asfoundation.wallet.wallet_reward

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.asf.wallet.R
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asfoundation.wallet.promotions.ui.vip_referral.PromotionsVipReferralFragment
import com.asfoundation.wallet.ui.gamification.GamificationActivity
import com.asfoundation.wallet.ui.settings.entry.SettingsFragment
import javax.inject.Inject

class RewardNavigator @Inject constructor(
  private val fragment: Fragment,
  private val navController: NavController
) : Navigator {

  fun navigateToSettings(
    mainNavController: NavController,
    turnOnFingerprint: Boolean = false
  ) {
    val bundle = Bundle()
    bundle.putBoolean(SettingsFragment.TURN_ON_FINGERPRINT, turnOnFingerprint)
    mainNavController.navigate(resId = R.id.action_navigate_to_settings, args = bundle)
  }

  fun showPromoCodeFragment() {
    navigate(
      navController,
      RewardFragmentDirections.actionNavigatePromoCode()
    )
  }

  fun showGiftCardFragment() {
    navigate(
      navController,
      RewardFragmentDirections.actionNavigateGiftCard()
    )
  }

  fun navigateToWithdrawScreen() {
    navigate(
      navController,
      RewardFragmentDirections.actionNavigateEskillsWithdraw()
    )
  }

  fun navigateToGamification(cachedBonus: Double) {
    fragment.startActivity(GamificationActivity.newIntent(fragment.requireContext(), cachedBonus))
  }

  fun navigateToVipReferral(
    bonus: String,
    code: String,
    totalEarned: String,
    numberReferrals: String,
    mainNavController: NavController
  ) {
    val bundle = Bundle()
    bundle.putString(PromotionsVipReferralFragment.BONUS_PERCENT, bonus)
    bundle.putString(PromotionsVipReferralFragment.PROMO_REFERRAL, code)
    bundle.putString(PromotionsVipReferralFragment.EARNED_VALUE, totalEarned)
    bundle.putString(PromotionsVipReferralFragment.EARNED_TOTAL, numberReferrals)
    mainNavController.navigate(R.id.action_navigate_to_vip_referral, bundle)
  }

}
