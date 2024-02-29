package com.appcoins.wallet.core.network.eskills.model

import com.google.gson.annotations.SerializedName

class FirstTimeUserResponse(
  @SerializedName("first_time_user")
  var firstTimeUserCheck: Boolean
)