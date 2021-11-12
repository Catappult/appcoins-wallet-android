package com.asfoundation.wallet.promo_code.repository

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class PromoCodeResponse(
    @JsonProperty("code") @SerializedName("code") val code: String,
    @JsonProperty("expiry") @SerializedName("expiry") val expiry: String?,
    @JsonProperty("expired") @SerializedName("expired") val expired: Boolean? = null)