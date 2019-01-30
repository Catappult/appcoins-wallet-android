package com.appcoins.wallet.bdsbilling.repository.entity


data class Transaction(val uid: String,
                       val status: Status,
                       val gateway: Gateway?,
                       var hash: String?,
                       val orderReference: String?) {
  companion object {
    fun notFound(): Transaction {
      return Transaction("", Status.INVALID_TRANSACTION, Gateway.unknown(), null, null)
    }

  }

  enum class Status {
    PENDING, PENDING_SERVICE_AUTHORIZATION, PROCESSING, COMPLETED, INVALID_TRANSACTION, FAILED,
    CANCELED
  }

}