package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName

data class FetchPublicKeyResponse(
  @SerializedName("public_key") val publicKey: String,
)
