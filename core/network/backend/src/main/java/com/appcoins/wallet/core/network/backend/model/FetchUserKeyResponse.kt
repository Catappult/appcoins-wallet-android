package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName

data class FetchUserKeyResponse(
  @SerializedName("user_key") val userKey: String,
)
