package com.appcoins.wallet.appcoins.rewards

import java.math.BigDecimal

data class Transaction(
  val sku: String?,
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
  val guestWalletId: String?,
  val errorCode: Int? = null,
  val errorMessage: String? = null
) {
  constructor(
    transaction: Transaction, status: Status, errorCode: Int? = null,
    errorMessage: String? = null
  ) : this(
    sku = transaction.sku,
    type = transaction.type,
    developerAddress = transaction.developerAddress,
    entityOemId = transaction.entityOemId,
    entityDomain = transaction.entityDomain,
    packageName = transaction.packageName,
    amount = transaction.amount,
    origin = transaction.origin,
    status = status,
    txId = transaction.txId,
    purchaseUid = transaction.purchaseUid,
    payload = transaction.payload,
    callback = transaction.callback,
    orderReference = transaction.orderReference,
    referrerUrl = transaction.referrerUrl,
    productToken = transaction.productToken,
    guestWalletId = transaction.guestWalletId,
    errorCode = errorCode,
    errorMessage = errorMessage
  )

  fun isBds(): Boolean = this.origin == "BDS" || this.origin == "UNITY"

  enum class Status {
    PENDING, PROCESSING, COMPLETED, ERROR, FORBIDDEN, SUB_ALREADY_OWNED, NO_NETWORK
  }
}
