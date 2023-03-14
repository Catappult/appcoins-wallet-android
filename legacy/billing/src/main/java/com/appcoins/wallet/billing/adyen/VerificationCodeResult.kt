package com.appcoins.wallet.billing.adyen

import com.appcoins.wallet.billing.util.Error

data class VerificationCodeResult(
    val success: Boolean,
    val errorType: ErrorType? = null,
    val error: Error = Error()) {

  enum class ErrorType { OTHER, TOO_MANY_ATTEMPTS, WRONG_CODE }
}

