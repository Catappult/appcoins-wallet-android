package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BonusHistory(
  @SerializedName("date")
  @Expose
  val date: String,

  @SerializedName("users")
  @Expose
  val users: List<BonusUser>
)