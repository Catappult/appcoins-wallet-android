package com.appcoins.wallet.billing.adyen

data class VerificationTransactionResponse(
    val code: String?,
    val data: VerificationData?
)

data class VerificationData(
    val pspReference: String?,
    val refusalReason: String?,
    val resultCode: String?,
    val refusalReasonCode: String?,
    val merchantReference: String?
)