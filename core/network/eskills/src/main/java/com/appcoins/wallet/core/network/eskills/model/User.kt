package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

class User(
  @SerializedName("wallet_address")
  var walletAddress: String,

  @SerializedName("user_name")
  var userName: String,

  @SerializedName("score")
  var score: Int
)
