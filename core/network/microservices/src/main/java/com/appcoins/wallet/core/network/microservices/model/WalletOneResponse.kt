package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName


data class WalletOneResponse(
  val uid: String,
  val hash: String?,
  val status: TransactionStatus,
  @SerializedName("html") val htmlData: String?,
  val data: ErrorData?,
) {
  fun mapValidity(): WalletOneTransaction.WalletOneValidityState {
    return when (status) {
      TransactionStatus.PENDING -> WalletOneTransaction.WalletOneValidityState.PENDING
      TransactionStatus.PENDING_SERVICE_AUTHORIZATION -> WalletOneTransaction.WalletOneValidityState.PENDING
      TransactionStatus.SETTLED -> WalletOneTransaction.WalletOneValidityState.PENDING
      TransactionStatus.PROCESSING -> WalletOneTransaction.WalletOneValidityState.PENDING
      TransactionStatus.COMPLETED -> WalletOneTransaction.WalletOneValidityState.COMPLETED
      TransactionStatus.PENDING_USER_PAYMENT -> WalletOneTransaction.WalletOneValidityState.PENDING
      TransactionStatus.INVALID_TRANSACTION -> WalletOneTransaction.WalletOneValidityState.ERROR
      TransactionStatus.FAILED -> WalletOneTransaction.WalletOneValidityState.ERROR
      TransactionStatus.CANCELED -> WalletOneTransaction.WalletOneValidityState.ERROR
      TransactionStatus.FRAUD -> WalletOneTransaction.WalletOneValidityState.ERROR
      TransactionStatus.PENDING_VALIDATION -> WalletOneTransaction.WalletOneValidityState.PENDING
      TransactionStatus.PENDING_CODE -> WalletOneTransaction.WalletOneValidityState.PENDING
      TransactionStatus.VERIFIED -> WalletOneTransaction.WalletOneValidityState.COMPLETED
      TransactionStatus.EXPIRED -> WalletOneTransaction.WalletOneValidityState.ERROR
    }
  }

  data class ErrorData(
    val name: String?,
    val message: String?,
  )

}
