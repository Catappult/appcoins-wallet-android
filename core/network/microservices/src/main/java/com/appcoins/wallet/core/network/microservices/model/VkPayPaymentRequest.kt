package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName


data class VkPayPaymentRequest(
  @SerializedName("callback_url") val callbackUrl: String?,
  @SerializedName("domain") val domain: String?,
  @SerializedName("metadata") val metadata: String?,
  @SerializedName("origin") val origin: String?,
  @SerializedName("product") val sku: String?,
  @SerializedName("reference") val reference: String?,
  @SerializedName("type") val type: String?,
  @SerializedName("price") val price: VkPrice,
  @SerializedName("wallets.developer") val developer: String?,
  @SerializedName("entity.oemid") val entityOemId: String?,
  @SerializedName("entity.domain") val entityDomain: String?,
  @SerializedName("entity.promo_code") val entityPromoCode: String?,
  @SerializedName("wallets.user") val user: String?,
  @SerializedName("order_id") val orderId: String?,
  @SerializedName("referrer_url") val referrerUrl: String?
)

data class VkPrice(
  val currency: String,
  val value: String
)
