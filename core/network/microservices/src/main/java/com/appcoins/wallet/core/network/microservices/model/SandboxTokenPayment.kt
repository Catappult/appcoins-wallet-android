package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class SandboxTokenPayment(
  @SerializedName("callback_url") val callbackUrl: String?,
  @SerializedName("metadata") val metadata: String?,
  @SerializedName("origin") val origin: String?,
  @SerializedName("reference") val reference: String?,
  @SerializedName("entity.oemid") val entityOemId: String?,
  @SerializedName("entity.domain") val entityDomain: String?,
  @SerializedName("entity.promo_code") val entityPromoCode: String?,
  @SerializedName("wallets.user") val user: String?,
  @SerializedName("referrer_url") val referrerUrl: String?,
  @SerializedName("entity.guest_id") val guestWalletId: String?,
  @SerializedName("product_token") val productToken: String?
)