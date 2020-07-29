package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PaymentMethodEntity(@SerializedName("name") val id: String, val label: String,
                               @SerializedName("icon") val iconUrl: String,
                               @SerializedName("status")
                               val availability: String,
                               val gateway: Gateway,
                               val fee: FeeEntity?)

data class FeeEntity(val exact: Boolean, val value: BigDecimal, val currency: String)