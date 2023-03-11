package com.appcoins.wallet.bdsbilling.exceptions

open class BillingException(private val errorCode: Int) : Exception() {
  fun getErrorCode(): Int {
    return errorCode
  }
}