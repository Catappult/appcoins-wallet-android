package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

data class NextPrizeSchedule (
  @SerializedName("next_schedule")
  val nextSchedule: Long
)