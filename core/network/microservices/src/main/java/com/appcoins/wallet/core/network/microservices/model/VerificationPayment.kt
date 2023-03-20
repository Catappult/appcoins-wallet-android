package com.appcoins.wallet.core.network.microservices.model

import com.adyen.checkout.core.model.ModelObject
import com.google.gson.annotations.SerializedName

data class VerificationPayment(
  @SerializedName("payment.method") val adyenPaymentMethod: ModelObject,
  @SerializedName("payment.store_method") val shouldStoreMethod: Boolean,
  @SerializedName("payment.return_url") val returnUrl: String
)