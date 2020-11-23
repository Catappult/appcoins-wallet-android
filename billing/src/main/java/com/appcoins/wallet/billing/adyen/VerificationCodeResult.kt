package com.appcoins.wallet.billing.adyen

import com.appcoins.wallet.billing.util.Error

data class VerificationCodeResult(
    val success: Boolean,
    val error: Error = Error()
)