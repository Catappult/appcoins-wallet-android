package com.appcoins.wallet.billing.adyen

data class VerificationInfoResponse(
    val currency: String,
    val symbol: String,
    val value: String,
    val digits: Int,
    val format: String,
    val period: String
)