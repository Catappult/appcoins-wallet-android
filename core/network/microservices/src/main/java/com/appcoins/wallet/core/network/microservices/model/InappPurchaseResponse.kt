package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class InappPurchaseResponse(
  val uid: String,
  val sku: String,
  val state: PurchaseState,
  @SerializedName("order_uid")
  val orderUid: String,
  val payload: String?,
  val created: String,
  val verification: Verification
)

data class Verification(val type: String, val data: String, val signature: String)

enum class PurchaseState {
  PENDING, //The subscription purchase is pending acknowledgement.
  ACKNOWLEDGED, //The subscription purchase has been acknowledged.
  CONSUMED //The subscription purchase has been consumed.
}
