package com.asfoundation.wallet.wallet_verification.code

data class VerificationCodeData(
    val transDate: String,
    val description: String,
    val amount: String,
    val currency: String
)