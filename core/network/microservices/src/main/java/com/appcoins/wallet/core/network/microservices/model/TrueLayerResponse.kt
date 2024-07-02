package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class TrueLayerResponse(
  val uid: String,
  val hash: String?,
  val status: TransactionStatus,
  @SerializedName("payment_id") val paymentId: String?,
  @SerializedName("resource_token") val resourceToken: String?,
  val data: ErrorData?,
) {
  fun mapValidity(): TrueLayerTransaction.TrueLayerValidityState {
    return when (status) {
      TransactionStatus.PENDING -> TrueLayerTransaction.TrueLayerValidityState.PENDING
      TransactionStatus.PENDING_SERVICE_AUTHORIZATION -> TrueLayerTransaction.TrueLayerValidityState.PENDING
      TransactionStatus.SETTLED -> TrueLayerTransaction.TrueLayerValidityState.PENDING
      TransactionStatus.PROCESSING -> TrueLayerTransaction.TrueLayerValidityState.PENDING
      TransactionStatus.COMPLETED -> TrueLayerTransaction.TrueLayerValidityState.COMPLETED
      TransactionStatus.PENDING_USER_PAYMENT -> TrueLayerTransaction.TrueLayerValidityState.PENDING
      TransactionStatus.INVALID_TRANSACTION -> TrueLayerTransaction.TrueLayerValidityState.ERROR
      TransactionStatus.FAILED -> TrueLayerTransaction.TrueLayerValidityState.ERROR
      TransactionStatus.CANCELED -> TrueLayerTransaction.TrueLayerValidityState.ERROR
      TransactionStatus.FRAUD -> TrueLayerTransaction.TrueLayerValidityState.ERROR
      TransactionStatus.PENDING_VALIDATION -> TrueLayerTransaction.TrueLayerValidityState.PENDING
      TransactionStatus.PENDING_CODE -> TrueLayerTransaction.TrueLayerValidityState.PENDING
      TransactionStatus.VERIFIED -> TrueLayerTransaction.TrueLayerValidityState.COMPLETED
      TransactionStatus.EXPIRED -> TrueLayerTransaction.TrueLayerValidityState.ERROR
    }
  }

  data class ErrorData(
    val name: String?,
    val message: String?,
  )

}
