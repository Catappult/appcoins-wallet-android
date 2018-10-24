package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

data class Purchase(val uid: String, val product: RemoteProduct, val status: String,
                    val packageName: Package, val signature: Signature)

data class RemoteProduct(val name: String)

data class Package(val name: String)

data class Signature(val value: String, val message: DeveloperPurchase)

class PurchaseSignatureSerializer : JsonSerializer<Purchase> {
  override fun serialize(purchase: Purchase?, typeOfSrc: Type?,
                         context: JsonSerializationContext?): JsonElement {

    val json = JsonObject()
    val signature = purchase?.signature?.message
    json.add("orderId", context?.serialize(signature?.orderId))
    json.add("packageName", context?.serialize(signature?.packageName))
    json.add("productId", context?.serialize(signature?.productId))
    json.add("purchaseTime", context?.serialize(signature?.purchaseTime))
    json.add("purchaseToken", context?.serialize(signature?.purchaseToken))
    json.add("purchaseState", context?.serialize(signature?.purchaseState))
    json.add("developerPayload", context?.serialize(signature?.developerPayload))
    return json
  }
}