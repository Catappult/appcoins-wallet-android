package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

data class TopNPlayersResponse(
  @SerializedName("current_user")
  val currentUser: TopRankings? = null,

  @SerializedName("user_list")
  val userList: List<TopRankings>
)