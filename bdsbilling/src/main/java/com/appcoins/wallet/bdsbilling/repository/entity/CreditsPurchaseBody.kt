package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.annotations.SerializedName

class CreditsPurchaseBody(

    @SerializedName("callback_url") val callback: String?,
    @SerializedName("product_token") val productToken: String?
)