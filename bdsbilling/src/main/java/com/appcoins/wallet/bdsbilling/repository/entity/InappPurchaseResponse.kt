package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class InappPurchaseResponse(val uid: String, val product: RemoteProductEntity,
                                 val status: String,
                                 @SerializedName("package")
                                 val packageName: PackageEntity,
                                 val signature: SignatureEntity)

data class RemoteProductEntity(val name: String)

data class PackageEntity(val name: String)

data class SignatureEntity(val value: String, val message: DeveloperPurchase)

class PurchaseSignatureEntitySerializer : JsonSerializer<InappPurchaseResponse> {
  override fun serialize(purchase: InappPurchaseResponse?, typeOfSrc: Type?,
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