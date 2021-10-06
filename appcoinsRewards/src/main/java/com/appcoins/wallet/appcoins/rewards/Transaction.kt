package com.appcoins.wallet.appcoins.rewards

import java.math.BigDecimal

data class Transaction(val sku: String?,
                       val type: String,
                       val developerAddress: String,
                       val entityOemId: String?,
                       val entityDomain: String?,
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
                       val productToken: String?,
                       val errorCode: Int? = null,
                       val errorMessage: String? = null) {
  constructor(transaction: Transaction, status: Status, errorCode: Int? = null,
              errorMessage: String? = null) : this(transaction.sku, transaction.type,
      transaction.developerAddress, transaction.entityOemId, transaction.entityDomain,
      transaction.packageName, transaction.amount, transaction.origin, status, transaction.txId,
      transaction.purchaseUid, transaction.payload, transaction.callback,
      transaction.orderReference, transaction.referrerUrl, transaction.productToken, errorCode, errorMessage)

  fun isBds(): Boolean = this.origin == "BDS" || this.origin == "UNITY"

  enum class Status {
    PENDING, PROCESSING, COMPLETED, ERROR, FORBIDDEN, SUB_ALREADY_OWNED, NO_NETWORK
  }
}
