package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName

data class VipReferralResponse(
  @SerializedName("code") val code: String,
  @SerializedName("earned_usd_amount") val earnedUsdAmount: String,
  @SerializedName("referrals") val referrals: String,
  @SerializedName("active") val active: Boolean,
  @SerializedName("revenue_share") val vipBonus: String
) {

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

