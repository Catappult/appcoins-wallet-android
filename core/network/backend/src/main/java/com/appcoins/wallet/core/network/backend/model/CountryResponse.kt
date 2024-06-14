package com.appcoins.wallet.core.network.backend.model


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CountryResponse(
  @SerializedName("countryCode", alternate = ["country_code"]) val countryCode: String? = null,
  @SerializedName("show_refund_disclamer") val showRefundDisclaimer: Int? = null,
) : Serializable