package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName


data class AmazonPayPaymentRequest(
  @SerializedName("callback_url") val callbackUrl: String?,
  @SerializedName("domain") val domain: String?,
  @SerializedName("product") val sku: String?,
  @SerializedName("type") val type: String?,
  @SerializedName("price") val price: AmazonPrice,
  @SerializedName("referrer_url") val referrerUrl: String?,
  @SerializedName("return_url") val returnUrl: String?,
  @SerializedName("method") val method: String?,
  @SerializedName("channel") val channel: String?,
)

data class AmazonPrice(
  val currency: String,
  val value: String
)
