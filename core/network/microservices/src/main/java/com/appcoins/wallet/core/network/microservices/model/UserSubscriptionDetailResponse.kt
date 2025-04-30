package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class UserSubscriptionDetailResponse(
    val sku: String,
    val period: String,
    val title: String?,
    val description: String?,
    val price: PriceResponse
)

data class PriceResponse(
    val currency: String,
    val value: String,
    val label: String,
    val symbol: String?,
    val micros: Long?,
    val trial: TrialResponse?
)

data class TrialResponse(
    val period: String?,
    @SerializedName("end_date")
    val endDate: String?
)
