package com.appcoins.wallet.appcoins.rewards

import java.math.BigDecimal

data class Transaction(val sku: String?,
                       val type: String,
                       val developerAddress: String,
                       val storeAddress: String,
                       val oemAddress: String,
                       val packageName: String,
                       val amount: BigDecimal,
                       val origin: String,
                       val status: Status,
                       var txId: String?,
                       val payload: String?,
                       val callback: String?,
                       val orderReference: String?) {
  constructor(transaction: Transaction, status: Status) : this(transaction.sku, transaction.type,
      transaction.developerAddress, transaction.oemAddress, transaction.storeAddress,
      transaction.packageName, transaction.amount, transaction.origin, status, transaction.txId,
      transaction.payload, transaction.callback, transaction.orderReference)

  fun isBds(): Boolean = this.origin == "BDS" || this.origin == "UNITY"

  enum class Status {
    PENDING, PROCESSING, COMPLETED, ERROR, NO_NETWORK
  }
}
