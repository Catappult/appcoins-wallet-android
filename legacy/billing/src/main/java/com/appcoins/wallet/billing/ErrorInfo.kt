package com.appcoins.wallet.billing

import java.io.Serializable

data class ErrorInfo(
  val httpCode: Int? = null, val messageCode: String? = null,
  val text: String? = null,
  val errorType: ErrorType? = ErrorType.UNKNOWN
) : Serializable {

  enum class ErrorType {
    BLOCKED,
    SUB_ALREADY_OWNED,
    CONFLICT, UNKNOWN,
    INVALID_CARD,
    PAYMENT_ERROR,
    CARD_SECURITY_VALIDATION,
    ALREADY_PROCESSED,
    OUTDATED_CARD,
    INVALID_COUNTRY_CODE,
    PAYMENT_NOT_SUPPORTED_ON_COUNTRY,
    CURRENCY_NOT_SUPPORTED,
    CVC_LENGTH,
    TRANSACTION_AMOUNT_EXCEEDED,
    CVC_REQUIRED,
  }
}
