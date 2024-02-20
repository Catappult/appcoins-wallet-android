package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class GetGooglePayUrlResponse(
    val url: String,
    @SerializedName("return_url") val returnUrl: String,
)
