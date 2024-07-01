package com.appcoins.wallet.core.network.base.compat


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WalletEmailRequest(
  @SerializedName("email") val email: String? = null
) : Serializable