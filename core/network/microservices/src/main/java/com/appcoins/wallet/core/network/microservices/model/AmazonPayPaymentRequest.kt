package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName


data class AmazonPayPaymentRequest(
  @SerializedName("callback_url") val callbackUrl: String?,
  @SerializedName("domain") val domain: String?,
  @SerializedName("metadata") val metadata: String?,
  @SerializedName("origin") val origin: String?,
  @SerializedName("product") val sku: String?,
  @SerializedName("reference") val reference: String?,
  @SerializedName("type") val type: String?,
  @SerializedName("price") val price: AmazonPrice,
  @SerializedName("entity.oemid") val entityOemId: String?,
  @SerializedName("entity.domain") val entityDomain: String?,
  @SerializedName("entity.promo_code") val entityPromoCode: String?,
  @SerializedName("referrer_url") val referrerUrl: String?,
  @SerializedName("return_url") val returnUrl: String?,
  @SerializedName("entity.guest_id") val guestWalletId: String?,
  @SerializedName("method") val method: String?,
  @SerializedName("channel") val channel: String?,
  @SerializedName("charge_permission_id") val chargePermissionId: String?,
)

data class AmazonPrice(
  val currency: String,
  val value: String
)
