package com.appcoins.wallet.billing.adyen

import com.appcoins.wallet.billing.util.Error

data class VerificationPaymentModel(val success: Boolean, val errorType: ErrorType?,
                                    val refusalReason: String?, val refusalCode: Int?,
                                    val error: Error = Error()) {

  enum class ErrorType { OTHER, TOO_MANY_ATTEMPTS, INVALID_REQUEST }
}
