package com.appcoins.wallet.appcoins.rewards

data class ErrorInfo(val errorType: ErrorType, val errorCode: Int?,
                     val errorMessage: String?) {

  enum class ErrorType {
    BLOCKED, SUB_ALREADY_OWNED, CONFLICT, NO_NETWORK, UNKNOWN
  }
}