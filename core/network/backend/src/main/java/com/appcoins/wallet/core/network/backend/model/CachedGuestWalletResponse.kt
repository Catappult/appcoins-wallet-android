package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName

data class CachedGuestWalletResponse(
  @SerializedName("private_key") val privateKey: String?,
  @SerializedName("flow") val flow: String?,
  @SerializedName("payment_funnel") val paymentFunnel: String?,
)
