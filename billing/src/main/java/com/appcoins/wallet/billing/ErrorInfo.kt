package com.appcoins.wallet.billing

data class ErrorInfo(val httpCode: Int? = null, val messageCode: String? = null,
                     val text: String? = null,
                     val errorType: ErrorType? = ErrorType.UNKNOWN) {

  enum class ErrorType {
    BILLING_ADDRESS, BLOCKED, SUB_ALREADY_OWNED, CONFLICT, UNKNOWN
  }
}
