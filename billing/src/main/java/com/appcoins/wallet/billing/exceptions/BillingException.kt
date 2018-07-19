package com.appcoins.wallet.billing.exceptions

internal open class BillingException(private val errorCode: Int) : Exception() {
  fun getErrorCode(): Int {
    return errorCode
  }
}