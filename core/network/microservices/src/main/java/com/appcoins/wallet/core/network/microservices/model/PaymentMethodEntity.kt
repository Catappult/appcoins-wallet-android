package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PaymentMethodEntity(@SerializedName("name") val id: String, val label: String,
                               @SerializedName("icon") val iconUrl: String,
                               @SerializedName("status")
                               val availability: String,
                               val gateway: Gateway,
                               val async: Boolean,
                               val fee: FeeEntity?) {

  fun isAvailable(): Boolean = this.availability != "UNAVAILABLE"
}

data class FeeEntity(val type: FeeType, val cost: FeeCost?)

enum class FeeType {
  EXACT, UNKNOWN
}

data class FeeCost(val value: BigDecimal, val currency: String)