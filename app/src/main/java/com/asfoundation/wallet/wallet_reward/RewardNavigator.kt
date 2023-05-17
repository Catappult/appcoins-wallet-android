package com.asfoundation.wallet.wallet_reward

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asfoundation.wallet.eskills.withdraw.WithdrawActivity
import com.asfoundation.wallet.promo_code.bottom_sheet.entry.PromoCodeBottomSheetFragment
import com.asfoundation.wallet.redeem_gift.bottom_sheet.RedeemGiftBottomSheetFragment
import com.asfoundation.wallet.ui.settings.SettingsActivity
import javax.inject.Inject

class RewardNavigator @Inject constructor(
  private val fragment: Fragment,
  private val navController: NavController
) : Navigator {

  fun navigateToSettings(turnOnFingerprint: Boolean = false) {
    val intent = SettingsActivity.newIntent(fragment.requireContext(), turnOnFingerprint)
    openIntent(intent)
  }

  fun openIntent(intent: Intent) = fragment.requireContext()
    .startActivity(intent)

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
    val intent = WithdrawActivity.newIntent(fragment.requireContext())
      .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK }
    fragment.requireContext().startActivity(intent)
  }
}
