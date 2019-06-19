package com.asfoundation.wallet.repository

import com.asfoundation.wallet.repository.entity.OperationEntity
import com.asfoundation.wallet.repository.entity.TransactionDetailsEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity
import com.asfoundation.wallet.transactions.Operation
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionDetails

class TransactionMapper {

  fun map(
      transactions: List<TransactionEntity>): List<Transaction> {
    return transactions.map { map(it) }
  }

  private fun map(transaction: TransactionEntity): Transaction {
    return Transaction(transaction.transactionId, map(transaction.type),
        transaction.approveTransactionId, transaction.timeStamp, transaction.processedTime,
        map(transaction.status),
        transaction.value, transaction.from, transaction.to, map(transaction.details),
        transaction.currency, mapToOperations(transaction.operations))
  }

  private fun mapToOperations(operations: List<OperationEntity>?): List<Operation>? {
    return operations?.map { Operation(it.transactionId, it.from, it.to, it.fee) }
  }

  private fun map(details: TransactionDetailsEntity?): TransactionDetails? {
    if (details == null) {
      return null
    }
    return TransactionDetails(details.sourceName, map(details.icon), details.description)
  }

  private fun map(icon: TransactionDetailsEntity.Icon): TransactionDetails.Icon {
    return TransactionDetails.Icon(map(icon.iconType), icon.uri)
  }

  private fun map(type: TransactionDetailsEntity.Type): TransactionDetails.Icon.Type {
    return when (type) {
      TransactionDetailsEntity.Type.FILE -> TransactionDetails.Icon.Type.FILE
      TransactionDetailsEntity.Type.URL -> TransactionDetails.Icon.Type.URL
    }
  }

  private fun map(status: TransactionEntity.TransactionStatus): Transaction.TransactionStatus {
    return when (status) {
      TransactionEntity.TransactionStatus.SUCCESS -> Transaction.TransactionStatus.SUCCESS
      TransactionEntity.TransactionStatus.FAILED -> Transaction.TransactionStatus.FAILED
      TransactionEntity.TransactionStatus.PENDING -> Transaction.TransactionStatus.PENDING
    }
  }

  private fun map(type: TransactionEntity.TransactionType): Transaction.TransactionType {
    return when (type) {
      TransactionEntity.TransactionType.STANDARD -> Transaction.TransactionType.STANDARD
      TransactionEntity.TransactionType.IAB -> Transaction.TransactionType.IAB
      TransactionEntity.TransactionType.ADS -> Transaction.TransactionType.ADS
      TransactionEntity.TransactionType.IAP_OFFCHAIN -> Transaction.TransactionType.IAP_OFFCHAIN
      TransactionEntity.TransactionType.ADS_OFFCHAIN -> Transaction.TransactionType.ADS_OFFCHAIN
      TransactionEntity.TransactionType.BONUS -> Transaction.TransactionType.BONUS
      TransactionEntity.TransactionType.TOP_UP -> Transaction.TransactionType.TOP_UP
      TransactionEntity.TransactionType.TRANSFER_OFF_CHAIN -> Transaction.TransactionType.TRANSFER_OFF_CHAIN
    }
  }

  fun map(transaction: Transaction, relatedWallet: String): TransactionEntity {
    return TransactionEntity(transaction.transactionId, relatedWallet,
        transaction.approveTransactionId,
        map(transaction.type), transaction.timeStamp, transaction.processedTime,
        map(transaction.status), transaction.value,
        transaction.from,
        transaction.to, map(transaction.details), transaction.currency,
        mapToOperationEntities(transaction.operations))
  }

  private fun mapToOperationEntities(operations: List<Operation>?): List<OperationEntity>? {
    return operations?.map { map(it) }
  }

  private fun map(operation: Operation): OperationEntity {
    return OperationEntity(operation.transactionId, operation.from, operation.to, operation.fee)
  }

  private fun map(details: TransactionDetails?): TransactionDetailsEntity? {
    if (details == null) {
      return null
    }
    return TransactionDetailsEntity(map(details.icon), details.sourceName, details.description)
  }

  private fun map(icon: TransactionDetails.Icon): TransactionDetailsEntity.Icon {
    return TransactionDetailsEntity.Icon(map(icon.type), icon.uri)
  }

  private fun map(type: TransactionDetails.Icon.Type): TransactionDetailsEntity.Type {
    return when (type) {
      TransactionDetails.Icon.Type.FILE -> TransactionDetailsEntity.Type.FILE
      TransactionDetails.Icon.Type.URL -> TransactionDetailsEntity.Type.URL
    }
  }

  private fun map(status: Transaction.TransactionStatus): TransactionEntity.TransactionStatus {
    return when (status) {
      Transaction.TransactionStatus.SUCCESS -> TransactionEntity.TransactionStatus.SUCCESS
      Transaction.TransactionStatus.FAILED -> TransactionEntity.TransactionStatus.FAILED
      Transaction.TransactionStatus.PENDING -> TransactionEntity.TransactionStatus.PENDING
    }
  }

  private fun map(type: Transaction.TransactionType): TransactionEntity.TransactionType {
    return when (type) {
      Transaction.TransactionType.STANDARD -> TransactionEntity.TransactionType.STANDARD
      Transaction.TransactionType.IAB -> TransactionEntity.TransactionType.IAB
      Transaction.TransactionType.ADS -> TransactionEntity.TransactionType.ADS
      Transaction.TransactionType.IAP_OFFCHAIN -> TransactionEntity.TransactionType.IAP_OFFCHAIN
      Transaction.TransactionType.ADS_OFFCHAIN -> TransactionEntity.TransactionType.ADS_OFFCHAIN
      Transaction.TransactionType.BONUS -> TransactionEntity.TransactionType.BONUS
      Transaction.TransactionType.TOP_UP -> TransactionEntity.TransactionType.TOP_UP
      Transaction.TransactionType.TRANSFER_OFF_CHAIN -> TransactionEntity.TransactionType.TRANSFER_OFF_CHAIN
    }
  }
}