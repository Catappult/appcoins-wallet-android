package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.annotations.SerializedName

data class PaymentMethod(@SerializedName("name") val id: String, val label: String,
                         @SerializedName("icon") val iconUrl: String, @SerializedName("status")
                         val availability: String,
                         val gateway: Gateway)
