package com.appcoins.wallet.core.network.microservices.model


data class SandboxResponse(
  val uid: String,
  val hash: String?,
  val status: TransactionStatus,
  val data: ErrorData?,
) {
  fun mapValidity(): SandboxTransaction.SandboxValidityState {
    return when(status) {
      TransactionStatus.PENDING -> SandboxTransaction.SandboxValidityState.PENDING
      TransactionStatus.PENDING_SERVICE_AUTHORIZATION -> SandboxTransaction.SandboxValidityState.PENDING
      TransactionStatus.SETTLED -> SandboxTransaction.SandboxValidityState.PENDING
      TransactionStatus.PROCESSING -> SandboxTransaction.SandboxValidityState.PENDING
      TransactionStatus.COMPLETED -> SandboxTransaction.SandboxValidityState.COMPLETED
      TransactionStatus.PENDING_USER_PAYMENT -> SandboxTransaction.SandboxValidityState.PENDING
      TransactionStatus.INVALID_TRANSACTION -> SandboxTransaction.SandboxValidityState.ERROR
      TransactionStatus.FAILED -> SandboxTransaction.SandboxValidityState.ERROR
      TransactionStatus.CANCELED -> SandboxTransaction.SandboxValidityState.ERROR
      TransactionStatus.FRAUD -> SandboxTransaction.SandboxValidityState.ERROR
      TransactionStatus.PENDING_VALIDATION -> SandboxTransaction.SandboxValidityState.PENDING
      TransactionStatus.PENDING_CODE -> SandboxTransaction.SandboxValidityState.PENDING
      TransactionStatus.VERIFIED -> SandboxTransaction.SandboxValidityState.COMPLETED
      TransactionStatus.EXPIRED -> SandboxTransaction.SandboxValidityState.ERROR
    }
  }

  data class ErrorData(
    val name: String?,
    val message: String?,
  )

}
