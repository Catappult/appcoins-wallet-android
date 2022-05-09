package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

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

class PurchaseSignatureEntitySerializer : JsonSerializer<InappPurchaseResponse> {
  override fun serialize(
    purchase: InappPurchaseResponse?,
    typeOfSrc: Type?,
    context: JsonSerializationContext?
  ): JsonElement {
    val json = JsonObject()
//    val signature = purchase?.signature?.message
//    json.add("orderId", context?.serialize(signature?.orderId))
//    json.add("packageName", context?.serialize(signature?.packageName))
//    json.add("productId", context?.serialize(signature?.productId))
//    json.add("purchaseTime", context?.serialize(signature?.purchaseTime))
//    json.add("purchaseToken", context?.serialize(signature?.purchaseToken))
//    json.add("purchaseState", context?.serialize(signature?.purchaseState))
//    json.add("developerPayload", context?.serialize(signature?.developerPayload))
    return json
  }
}