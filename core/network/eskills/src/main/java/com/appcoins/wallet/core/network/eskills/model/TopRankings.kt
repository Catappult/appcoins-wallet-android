package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

class TopRankings(

  val username: String,

  @SerializedName("rank_position")
  val rankPosition: Int,

  @SerializedName("wallet_address")
  val walletAddress: String,

  val score: Double
)