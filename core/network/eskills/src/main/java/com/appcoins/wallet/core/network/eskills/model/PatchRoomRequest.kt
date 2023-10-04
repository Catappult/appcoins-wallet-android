package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

data class PatchRoomRequest(
  @SerializedName("score")
  val score: Long,
  @SerializedName("status")
  val status: UserStatus
)