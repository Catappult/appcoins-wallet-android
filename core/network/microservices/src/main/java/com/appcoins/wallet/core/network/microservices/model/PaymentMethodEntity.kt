package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import kotlin.random.Random

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

val emptyPaymentMethodEntity = PaymentMethodEntity(
  id = Random.nextInt(1, 100000).toString(),
  label = "Name",
  iconUrl = "",
  availability = "AVAILABLE",
  gateway = Gateway(
    name = Gateway.Name.appcoins,
    label = "Gateway label",
    icon = "Gateway icon"
  ),
  async = true,
  price = Value(
    value = BigDecimal(50.0),
    currency = "€"
  ),
  fee = FeeEntity(
    type = FeeType.EXACT,
    cost = Value(
      value = BigDecimal(50.0),
      currency = "€"
    )
  ),
  message = "Description",
)
