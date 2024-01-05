package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class SessionPaymentDetails(
//  @SerializedName("payment.method") val adyenPaymentMethod: ModelObject,  // TODO
//  @SerializedName("payment.store_method") val shouldStoreMethod: Boolean,
//  @SerializedName("payment.return_url") val returnUrl: String,
//  @SerializedName("payment.shopper_interaction") val shopperInteraction: String?,
  @SerializedName("callback_url") val callbackUrl: String?,
  @SerializedName("domain") val domain: String?,
  @SerializedName("metadata") val metadata: String?,
  @SerializedName("method") val method: String?,
  @SerializedName("origin") val origin: String?,
  @SerializedName("product") val sku: String?,
  @SerializedName("reference") val reference: String?,
  @SerializedName("type") val type: String?,
  @SerializedName("price.currency") val currency: String?,
  @SerializedName("price.value") val value: String?,
  @SerializedName("wallets.developer") val developer: String?,
  @SerializedName("entity.oemid") val entityOemId: String?,
  @SerializedName("entity.domain") val entityDomain: String?,
  @SerializedName("entity.promo_code") val entityPromoCode: String?,
  @SerializedName("wallets.user") val user: String?,
  @SerializedName("referrer_url") val referrerUrl: String?
)