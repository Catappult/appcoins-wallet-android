package com.asfoundation.wallet.promo_code.repository

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class PromoCodeBonusResponse(
    @JsonProperty("code") @SerializedName("code") val code: String,
    @JsonProperty("bonus") @SerializedName("bonus") val bonus: Double? = null)