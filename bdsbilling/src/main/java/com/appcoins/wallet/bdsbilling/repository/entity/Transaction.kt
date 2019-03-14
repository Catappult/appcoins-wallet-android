package com.appcoins.wallet.bdsbilling.repository.entity


data class Transaction(val uid: String,
                       val status: Status,
                       val gateway: Gateway?,
                       var hash: String?,
                       val orderReference: String?,
                       val price: Price?) {

  companion object {
    fun notFound(): Transaction {
      return Transaction("", Status.INVALID_TRANSACTION, Gateway.unknown(), null, null, null)
    }

  }

  enum class Status {
    PENDING, PENDING_SERVICE_AUTHORIZATION, PROCESSING, COMPLETED, INVALID_TRANSACTION, FAILED,
    CANCELED
  }

}

data class Price(val currency: String, val value: String, val appc: String)