package com.asfoundation.wallet.iab.payment_manager.domain

import kotlinx.coroutines.flow.Flow

interface Transaction {
  val hash: String
  val status: Flow<TransactionStatus>

  fun isEndingState(status: TransactionStatus): Boolean {
    return (status == TransactionStatus.COMPLETED
        || status == TransactionStatus.FAILED
        || status == TransactionStatus.CANCELED
        || status == TransactionStatus.INVALID_TRANSACTION
        || status == TransactionStatus.FRAUD)
  }
}

enum class TransactionStatus {
  PENDING,
  PENDING_SERVICE_AUTHORIZATION,
  SETTLED,
  PROCESSING,
  COMPLETED,
  PENDING_USER_PAYMENT,
  INVALID_TRANSACTION,
  FAILED,
  CANCELED,
  DUPLICATED,
  CHARGEBACK,
  REFUNDED,
  FRAUD,
  PENDING_VALIDATION,
  PENDING_CODE,
  VERIFIED,
  EXPIRED
}
