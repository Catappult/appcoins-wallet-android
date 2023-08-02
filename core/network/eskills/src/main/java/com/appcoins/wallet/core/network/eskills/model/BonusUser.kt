package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BonusUser(
  @SerializedName("rank")
  @Expose
  val rank: Long,

  @SerializedName("bonus_amount")
  @Expose
  val bonusAmount: Float,

  @SerializedName("user_name")
  @Expose
  val userName: String,

  @SerializedName("score")
  @Expose
  val score: Double,
)