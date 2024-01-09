package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class AdyenSessionResponse(
  val uid: String,
  val hash: String?,
  @SerializedName("reference") val orderReference: String?,
  val status: TransactionStatus,
  val session: MakePaymentSessionResponse?,
  val metadata: TransactionMetadata?
) {
  // TODO check
  fun mapValidity(): GooglePayWebTransaction.GooglePayWebValidityState {
    return when(status) {
      TransactionStatus.PENDING -> GooglePayWebTransaction.GooglePayWebValidityState.PENDING
      TransactionStatus.PENDING_SERVICE_AUTHORIZATION -> GooglePayWebTransaction.GooglePayWebValidityState.PENDING
      TransactionStatus.SETTLED -> GooglePayWebTransaction.GooglePayWebValidityState.PENDING
      TransactionStatus.PROCESSING -> GooglePayWebTransaction.GooglePayWebValidityState.PENDING
      TransactionStatus.COMPLETED -> GooglePayWebTransaction.GooglePayWebValidityState.COMPLETED
      TransactionStatus.PENDING_USER_PAYMENT -> GooglePayWebTransaction.GooglePayWebValidityState.PENDING
      TransactionStatus.INVALID_TRANSACTION -> GooglePayWebTransaction.GooglePayWebValidityState.ERROR
      TransactionStatus.FAILED -> GooglePayWebTransaction.GooglePayWebValidityState.ERROR
      TransactionStatus.CANCELED -> GooglePayWebTransaction.GooglePayWebValidityState.ERROR
      TransactionStatus.FRAUD -> GooglePayWebTransaction.GooglePayWebValidityState.ERROR
      TransactionStatus.PENDING_VALIDATION -> GooglePayWebTransaction.GooglePayWebValidityState.PENDING
      TransactionStatus.PENDING_CODE -> GooglePayWebTransaction.GooglePayWebValidityState.PENDING
      TransactionStatus.VERIFIED -> GooglePayWebTransaction.GooglePayWebValidityState.COMPLETED
      TransactionStatus.EXPIRED -> GooglePayWebTransaction.GooglePayWebValidityState.ERROR
    }
  }

  data class ErrorData(
    val name: String?,
    val message: String?,
  )

}
