package com.appcoins.wallet.appcoins.rewards

import java.math.BigDecimal

data class Transaction(val sku: String?,
                       val type: String,
                       val developerAddress: String,
                       val storeAddress: String,
                       val oemAddress: String,
                       val packageName: String,
                       val amount: BigDecimal,
                       val origin: String?,
                       val status: Status,
                       var txId: String?,
                       var purchaseUid: String?,
                       val payload: String?,
                       val callback: String?,
                       val orderReference: String?,
                       val referrerUrl: String?,
                       val errorCode: Int? = null,
                       val errorMessage: String? = null) {
  constructor(transaction: Transaction, status: Status, errorCode: Int? = null,
              errorMessage: String? = null) : this(transaction.sku, transaction.type,
      transaction.developerAddress, transaction.oemAddress, transaction.storeAddress,
      transaction.packageName, transaction.amount, transaction.origin, status, transaction.txId,
      transaction.purchaseUid, transaction.payload, transaction.callback,
      transaction.orderReference, transaction.referrerUrl, errorCode, errorMessage)

  fun isBds(): Boolean = this.origin == "BDS" || this.origin == "UNITY"

  enum class Status {
    PENDING, PROCESSING, COMPLETED, ERROR, FORBIDDEN, NO_NETWORK
  }
}
