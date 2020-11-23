package com.asfoundation.wallet.wallet_verification.code

data class VerificationCodeData(
    val date: Long,
    val format: String,
    val amount: String,
    val currency: String,
    val sign: String,
    val period: String,
    val digits: Int
)