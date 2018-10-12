package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type
import java.math.BigDecimal

data class Transaction(val sku: String,
                       val type: Type,
                       val developerAddress: String,
                       val storeAddress: String,
                       val oemAddress: String,
                       val packageName: String,
                       val amount: BigDecimal,
                       val origin: Origin,
                       val status: Status) {
  constructor(transaction: Transaction, status: Status) : this(transaction.sku, transaction.type,
      transaction.developerAddress, transaction.storeAddress, transaction.oemAddress,
      transaction.packageName, transaction.amount, transaction.origin, status)


  enum class Status {
    PENDING, PROCESSING, COMPLETED, ERROR
  }
}
