package com.appcoins.wallet.billing.adyen

import com.appcoins.wallet.billing.util.Error

data class VerificationPaymentModel(val success: Boolean, val errorType: ErrorType? = null,
                                    val refusalReason: String? = null, val refusalCode: Int? = null,
                                    val redirectUrl: String? = null, val error: Error = Error()) {

  enum class ErrorType { OTHER, TOO_MANY_ATTEMPTS, INVALID_REQUEST }
}
