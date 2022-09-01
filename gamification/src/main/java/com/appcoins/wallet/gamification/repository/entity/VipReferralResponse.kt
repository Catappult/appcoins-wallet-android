package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName

data class VipReferralResponse(
  val code: String?,
  @SerializedName("earned_usd_amount") val earnedUsdAmount: String?,
  val referrals: String?,
  val active: Boolean?,
  @SerializedName("revenue_share") val vipBonus: String?
) {

  fun isValid(): Boolean {
    return (code != null && active != null && active)
  }

  companion object {
    val invalidReferral = VipReferralResponse(
      "",
      "",
      "",
      active = false,
      ""
    )
  }
}

