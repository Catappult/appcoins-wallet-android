package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class AmazonPayTransaction(
  val uid: String?,
  val reference: String?,
  val status: TransactionStatus?,
  val payload: String?,
  @SerializedName("merchant_id")
  val merchantId: String?,
  @SerializedName("checkout_session_id")
  val checkoutSessionId: String?,
  @SerializedName("redirect_url")
  val redirectUrl: String?,
  val errorCode: String? = null,
  val errorContent: String? = null,
)
