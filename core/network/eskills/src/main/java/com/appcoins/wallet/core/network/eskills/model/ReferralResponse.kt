package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName


data class ReferralResponse(
  @SerializedName("referral_code")
  val referralCode: String,

  @SerializedName("count")
  val count: Int,

  @SerializedName("available")
  val available: Boolean
  )