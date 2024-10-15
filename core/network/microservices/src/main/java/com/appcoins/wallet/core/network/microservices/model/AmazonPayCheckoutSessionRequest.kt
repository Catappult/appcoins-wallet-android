package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName


data class AmazonPayCheckoutSessionRequest(
  @SerializedName("checkout_session_id") val checkoutSessionId: String,
)