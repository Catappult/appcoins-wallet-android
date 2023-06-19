package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TopRankings(
  @SerializedName("username")
  @Expose
  val username: String,

  @SerializedName("rank_position")
  @Expose
  val rankPosition: Int,

  @SerializedName("wallet_address")
  @Expose
  val walletAddress: String,

  @SerializedName("score")
  @Expose
  val score: Int
)