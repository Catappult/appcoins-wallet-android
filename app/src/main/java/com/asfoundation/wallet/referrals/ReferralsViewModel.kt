package com.asfoundation.wallet.referrals

import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import java.math.BigDecimal

data class ReferralsViewModel(
    val completed: Int, val link: String?, val invited: Boolean, val pendingAmount: BigDecimal,
    val amount: BigDecimal, val symbol: String, val maxAmount: BigDecimal,
    val minAmount: BigDecimal, val available: Int, val receivedAmount: BigDecimal,
    val userStatus: ReferralResponse.UserStatus?) {

  fun isRedeemed(): Boolean {
    return userStatus != null && userStatus == ReferralResponse.UserStatus.REDEEMED
  }

}
