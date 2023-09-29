package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

class CreditsPurchaseBody(
    @SerializedName("callback_url") val callback: String?,
    @SerializedName("product_token") val productToken: String?,
    @SerializedName("entity.oemid") val entityOemId: String?,
)