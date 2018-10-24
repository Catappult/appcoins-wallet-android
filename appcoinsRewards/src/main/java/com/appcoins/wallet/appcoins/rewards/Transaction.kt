package com.appcoins.wallet.appcoins.rewards

import java.math.BigDecimal

data class Transaction(val sku: String,
                       val type: String,
                       val developerAddress: String,
                       val storeAddress: String,
                       val oemAddress: String,
                       val packageName: String,
                       val amount: BigDecimal,
                       val origin: Origin,
                       val status: Status,
                       var txId: String?) {
  constructor(transaction: Transaction, status: Status) : this(transaction.sku, transaction.type,
      transaction.developerAddress, transaction.storeAddress, transaction.oemAddress,
          transaction.packageName, transaction.amount, transaction.origin, status, transaction.txId)


  enum class Origin {
    BDS, UNKNOWN;

    fun isBds(): Boolean = this == BDS
  }

  enum class Status {
    PENDING, PROCESSING, COMPLETED, ERROR, NO_NETWORK
  }
}
