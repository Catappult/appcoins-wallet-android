package com.asfoundation.wallet.billing.googlepay.models

import com.appcoins.wallet.core.network.microservices.model.GetGooglePayUrlResponse

data class GooglePayUrls(
  val url: String,
  val returnUrl: String,
) {

  companion object {
    fun map(response: GetGooglePayUrlResponse): GooglePayUrls {
      return GooglePayUrls(
        response.url,
        response.returnUrl,
      )
    }
  }

}
