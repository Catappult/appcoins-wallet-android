package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

class LoginResponse(
  @SerializedName("token")
  var token: String
)