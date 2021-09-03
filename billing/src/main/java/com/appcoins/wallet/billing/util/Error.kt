package com.appcoins.wallet.billing.util

data class Error(val hasError: Boolean = false, val isNetworkError: Boolean = false,
                 val code: Int? = null, val message: String? = null,
                 val errorType: ErrorType? = null) {
  enum class ErrorType {
    INVALID_CARD, PAYMENT_ERROR, CARD_SECURITY_VALIDATION, TIMEOUT, ALREADY_PROCESSED
  }
}

