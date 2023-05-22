package com.appcoins.wallet.core.network.microservices.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class FiatCurrenciesResponse(@JsonProperty("items") val items: List<CurrencyItem>)

data class CurrencyItem(
    @JsonProperty("currency") @SerializedName("currency") val currency: String,
    @JsonProperty("label") @SerializedName("label") val label: String?,
    @JsonProperty("sign") @SerializedName("sign") val sign: String?,
    @JsonProperty("flag") @SerializedName("flag") val flag: String?)



