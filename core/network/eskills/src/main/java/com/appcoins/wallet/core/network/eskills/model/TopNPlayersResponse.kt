package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TopNPlayersResponse(
  @SerializedName("current_user")
  @Expose
  val currentUser: TopRankings? = null,

  @SerializedName("user_list")
  @Expose
  val userList: Array<TopRankings>
)