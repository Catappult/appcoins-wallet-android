package com.asfoundation.wallet.entity


class SubmitPoAException(errorCode: Int = -1) : Exception() {
  val error = mapErrorCode(errorCode)

  private fun mapErrorCode(code: Int): Int {
    var errorCode = GENERIC_ERROR
    when (code) {
      0 -> errorCode = CAMPAIGN_NOT_EXISTENT
      1 -> errorCode = CAMPAIGN_NOT_AVAILABLE
      2 -> errorCode = NOT_ENOUGH_BUDGET
      3 -> errorCode = NOT_AVAILABLE_FOR_COUNTRY
      4 -> errorCode = ALREADY_SUBMITTED_FOR_IP
      5 -> errorCode = ALREADY_SUBMITTED_FOR_WALLET
      6 -> errorCode = INCORRECT_DATA
    }
    return errorCode
  }

  companion object {
    const val GENERIC_ERROR = -1
    const val CAMPAIGN_NOT_EXISTENT = 0
    const val CAMPAIGN_NOT_AVAILABLE = 1
    const val NOT_ENOUGH_BUDGET = 2
    const val NOT_AVAILABLE_FOR_COUNTRY = 3
    const val ALREADY_SUBMITTED_FOR_IP = 4
    const val ALREADY_SUBMITTED_FOR_WALLET = 5
    const val INCORRECT_DATA = 6
  }
}