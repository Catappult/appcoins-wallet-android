package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PaymentMethodEntity(
  @SerializedName("name") val id: String, val label: String,
  @SerializedName("icon") val iconUrl: String,
  @SerializedName("status")
  val availability: String,
  val gateway: Gateway,
  val async: Boolean,
  val price: Value,
  val fee: FeeEntity?,
  val message: String?,
) {

  fun isAvailable(): Boolean = this.availability != "UNAVAILABLE"
}

data class FeeEntity(val type: FeeType, val cost: Value?)

enum class FeeType {
  EXACT, UNKNOWN
}

data class Value(val value: BigDecimal, val currency: String)