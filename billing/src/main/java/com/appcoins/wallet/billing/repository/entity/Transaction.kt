package com.appcoins.wallet.billing.repository.entity


data class Transaction(val uid: String, val status: Status, val gateway: Gateway) {
  companion object {
    fun notFound(): Transaction {
      return Transaction("", Status.INVALID_TRANSACTION, Gateway.unknown())
    }

  }

  enum class Status {
    PENDING, PENDING_SERVICE_AUTHORIZATION, PROCESSING, COMPLETED, INVALID_TRANSACTION, FAILED,
    CANCELED
  }

}