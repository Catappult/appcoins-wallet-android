package com.appcoins.wallet.core.network.microservices.model


data class PaypalV2StartResponse(
  val uid: String,
  val hash: String?,
  val status: TransactionStatus,
  val data: ErrorData?,
) {
  fun mapValidity(): PaypalTransaction.PaypalValidityState {
    return when(status) {
      TransactionStatus.PENDING -> PaypalTransaction.PaypalValidityState.PENDING
      TransactionStatus.PENDING_SERVICE_AUTHORIZATION -> PaypalTransaction.PaypalValidityState.PENDING
      TransactionStatus.SETTLED -> PaypalTransaction.PaypalValidityState.PENDING
      TransactionStatus.PROCESSING -> PaypalTransaction.PaypalValidityState.PENDING
      TransactionStatus.COMPLETED -> PaypalTransaction.PaypalValidityState.COMPLETED
      TransactionStatus.PENDING_USER_PAYMENT -> PaypalTransaction.PaypalValidityState.PENDING
      TransactionStatus.INVALID_TRANSACTION -> PaypalTransaction.PaypalValidityState.ERROR
      TransactionStatus.FAILED -> PaypalTransaction.PaypalValidityState.ERROR
      TransactionStatus.CANCELED -> PaypalTransaction.PaypalValidityState.ERROR
      TransactionStatus.FRAUD -> PaypalTransaction.PaypalValidityState.ERROR
      TransactionStatus.PENDING_VALIDATION -> PaypalTransaction.PaypalValidityState.PENDING
      TransactionStatus.PENDING_CODE -> PaypalTransaction.PaypalValidityState.PENDING
      TransactionStatus.VERIFIED -> PaypalTransaction.PaypalValidityState.COMPLETED
      TransactionStatus.EXPIRED -> PaypalTransaction.PaypalValidityState.ERROR
    }
  }

  data class ErrorData(
    val name: String?,
    val message: String?,
  )

}
