package com.asfoundation.wallet.promotions.ui.vip_referral

import androidx.fragment.app.Fragment
import javax.inject.Inject


class PromotionsVipReferralNavigator @Inject constructor(val fragment: Fragment) {
  fun navigateBack() {
    fragment.requireActivity().onBackPressed()
  }
}
