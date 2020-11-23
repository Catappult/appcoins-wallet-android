package com.asfoundation.wallet.wallet_verification.code

data class VerificationCodeData(
    val date: Long,
    val format: String,
    val amount: String,
    val currency: String,
    val symbol: String,
    val period: String,
    val digits: Int
)