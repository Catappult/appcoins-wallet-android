package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName

data class CachedTransactionResponse(
  @SerializedName("url") val referrerUrl: String,
  val product: String,
  val domain: String,
  @SerializedName("callback_url") val callbackUrl: String,
  val currency: String,
  @SerializedName("order_reference") val orderReference: String,
  val value: Double,
  val signature: String,
  val origin: String,
  val type: String,
  @SerializedName("oem_id") val oemId: String?,
  @SerializedName("ws_port") val wsPort: String?,
  @SerializedName("sdk_version") val sdkVersion: String?,
  val metadata: String?
)