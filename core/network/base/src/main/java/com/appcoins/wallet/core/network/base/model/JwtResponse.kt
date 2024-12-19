package com.appcoins.wallet.core.network.base.model

import com.google.gson.annotations.SerializedName

data class JwtResponse(
  @SerializedName("jwt") private val _jwt: String
) {
  val jwt
    get() = "Bearer $_jwt"
}