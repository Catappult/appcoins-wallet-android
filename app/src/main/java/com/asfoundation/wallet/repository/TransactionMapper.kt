package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.utils.android_common.BalanceUtils
import com.asfoundation.wallet.repository.entity.OperationEntity
import com.asfoundation.wallet.repository.entity.TransactionDetailsEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity
import com.asfoundation.wallet.transactions.Operation
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionDetails
import java.math.BigDecimal
import javax.inject.Inject

class TransactionMapper @Inject constructor(){

  fun map(transactions: List<TransactionEntity>) = transactions.map { map(it) }

  fun map(transaction: TransactionEntity, link: TransactionEntity): Transaction {
    return Transaction(transaction.transactionId, map(transaction.type), map(transaction.subType),
        map(transaction.method), transaction.title, transaction.cardDescription,
        map(transaction.perk),
        transaction.approveTransactionId, transaction.timeStamp, transaction.processedTime,
        map(transaction.status), transaction.value,
        transaction.from, transaction.to, map(transaction.details),
        transaction.currency, mapToOperations(transaction.operations),
        listOf(mapLink(link, transaction)), transaction.paidAmount, transaction.paidCurrency,
        transaction.orderReference)
  }

  private fun map(method: TransactionEntity.Method?): Transaction.Method {
    return when (method) {
      TransactionEntity.Method.APPC -> Transaction.Method.APPC
      TransactionEntity.Method.APPC_C -> Transaction.Method.APPC_C
      TransactionEntity.Method.ETH -> Transaction.Method.ETH
      else -> Transaction.Method.UNKNOWN
    }
  }

  private fun mapLink(transaction: TransactionEntity, link: TransactionEntity): Transaction {
    return Transaction(transaction.transactionId, map(transaction.type), map(transaction.subType),
        map(transaction.method), transaction.title, transaction.cardDescription,
        map(transaction.perk), transaction.approveTransactionId, transaction.timeStamp,
        transaction.processedTime, map(transaction.status), transaction.value, transaction.from,
        transaction.to, map(transaction.details), transaction.currency,
        mapToOperations(transaction.operations), listOf(map(link)), transaction.paidAmount,
        transaction.paidCurrency, transaction.orderReference)
  }

  fun map(transaction: TransactionEntity): Transaction {
    return Transaction(transaction.transactionId, map(transaction.type), map(transaction.subType),
        map(transaction.method), transaction.title, transaction.cardDescription,
        map(transaction.perk), transaction.approveTransactionId, transaction.timeStamp,
        transaction.processedTime, map(transaction.status), transaction.value,
        transaction.from, transaction.to, map(transaction.details),
        transaction.currency, mapToOperations(transaction.operations),
        emptyList(), transaction.paidAmount, transaction.paidCurrency, transaction.orderReference)
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
      TransactionEntity.TransactionType.IAP -> Transaction.TransactionType.IAP
      TransactionEntity.TransactionType.ADS -> Transaction.TransactionType.ADS
      TransactionEntity.TransactionType.IAP_OFFCHAIN -> Transaction.TransactionType.IAP_OFFCHAIN
      TransactionEntity.TransactionType.ADS_OFFCHAIN -> Transaction.TransactionType.ADS_OFFCHAIN
      TransactionEntity.TransactionType.BONUS -> Transaction.TransactionType.BONUS
      TransactionEntity.TransactionType.TOP_UP -> Transaction.TransactionType.TOP_UP
      TransactionEntity.TransactionType.TRANSFER_OFF_CHAIN -> Transaction.TransactionType.TRANSFER_OFF_CHAIN
      TransactionEntity.TransactionType.TRANSFER -> Transaction.TransactionType.TRANSFER
      TransactionEntity.TransactionType.ETHER_TRANSFER -> Transaction.TransactionType.ETHER_TRANSFER
      TransactionEntity.TransactionType.TOP_UP_REVERT -> Transaction.TransactionType.TOP_UP_REVERT
      TransactionEntity.TransactionType.BONUS_REVERT -> Transaction.TransactionType.BONUS_REVERT
      TransactionEntity.TransactionType.IAP_REVERT -> Transaction.TransactionType.IAP_REVERT
      TransactionEntity.TransactionType.INAPP_SUBSCRIPTION -> Transaction.TransactionType.SUBS_OFFCHAIN
      TransactionEntity.TransactionType.ESKILLS_REWARD -> Transaction.TransactionType.ESKILLS_REWARD
      TransactionEntity.TransactionType.ESKILLS -> Transaction.TransactionType.ESKILLS
      TransactionEntity.TransactionType.CHALLENGE_REWARD -> Transaction.TransactionType.CHALLENGE_REWARD
    }
  }

  fun map(transaction: Transaction, relatedWallet: String): TransactionEntity {
    return TransactionEntity(transaction.transactionId, relatedWallet,
        transaction.approveTransactionId, map(transaction.perk),
        map(transaction.type), map(transaction.method), map(transaction.subType), transaction.title,
        transaction.description, transaction.timeStamp,
        transaction.processedTime, map(transaction.status), transaction.value, transaction.currency,
        transaction.paidAmount, transaction.paidCurrency,
        transaction.from, transaction.to, map(transaction.details),
        mapToOperationEntities(transaction.operations), transaction.orderReference)
  }

  private fun map(method: Transaction.Method): TransactionEntity.Method {
    return when (method) {
      Transaction.Method.UNKNOWN -> TransactionEntity.Method.UNKNOWN
      Transaction.Method.APPC -> TransactionEntity.Method.APPC
      Transaction.Method.APPC_C -> TransactionEntity.Method.APPC_C
      Transaction.Method.ETH -> TransactionEntity.Method.ETH
    }
  }

  private fun mapToOperationEntities(operations: List<Operation>?): List<OperationEntity>? {
    return operations?.map { map(it) }
  }

  private fun map(operation: Operation): OperationEntity {
    return OperationEntity(operation.transactionId, operation.from, operation.to,
        BalanceUtils.weiToEth(BigDecimal(operation.fee))
            .toPlainString())
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
      Transaction.TransactionType.IAP -> TransactionEntity.TransactionType.IAP
      Transaction.TransactionType.ADS -> TransactionEntity.TransactionType.ADS
      Transaction.TransactionType.IAP_OFFCHAIN -> TransactionEntity.TransactionType.IAP_OFFCHAIN
      Transaction.TransactionType.ADS_OFFCHAIN -> TransactionEntity.TransactionType.ADS_OFFCHAIN
      Transaction.TransactionType.BONUS -> TransactionEntity.TransactionType.BONUS
      Transaction.TransactionType.TOP_UP -> TransactionEntity.TransactionType.TOP_UP
      Transaction.TransactionType.TRANSFER_OFF_CHAIN -> TransactionEntity.TransactionType.TRANSFER_OFF_CHAIN
      Transaction.TransactionType.ETHER_TRANSFER -> TransactionEntity.TransactionType.ETHER_TRANSFER
      Transaction.TransactionType.BONUS_REVERT -> TransactionEntity.TransactionType.BONUS_REVERT
      Transaction.TransactionType.TOP_UP_REVERT -> TransactionEntity.TransactionType.TOP_UP_REVERT
      Transaction.TransactionType.IAP_REVERT -> TransactionEntity.TransactionType.IAP_REVERT
      Transaction.TransactionType.TRANSFER -> TransactionEntity.TransactionType.TRANSFER
      Transaction.TransactionType.SUBS_OFFCHAIN -> TransactionEntity.TransactionType.INAPP_SUBSCRIPTION
      Transaction.TransactionType.ESKILLS_REWARD -> TransactionEntity.TransactionType.ESKILLS_REWARD
      Transaction.TransactionType.ESKILLS -> TransactionEntity.TransactionType.ESKILLS
      Transaction.TransactionType.CHALLENGE_REWARD -> TransactionEntity.TransactionType.CHALLENGE_REWARD
    }
  }

  private fun map(subType: TransactionEntity.SubType?): Transaction.SubType? {
    if (subType == null) return null
    return when (subType) {
      TransactionEntity.SubType.PERK_PROMOTION -> Transaction.SubType.PERK_PROMOTION
      TransactionEntity.SubType.UNKNOWN -> Transaction.SubType.UNKNOWN
    }
  }

  private fun map(subType: Transaction.SubType?): TransactionEntity.SubType? {
    if (subType == null) return null
    return when (subType) {
      Transaction.SubType.PERK_PROMOTION -> TransactionEntity.SubType.PERK_PROMOTION
      Transaction.SubType.UNKNOWN -> TransactionEntity.SubType.UNKNOWN
    }
  }


  private fun map(perk: TransactionEntity.Perk?): Transaction.Perk? {
    if (perk == null) return null
    return when (perk) {
      TransactionEntity.Perk.GAMIFICATION_LEVEL_UP -> Transaction.Perk.GAMIFICATION_LEVEL_UP
      TransactionEntity.Perk.PACKAGE_PERK -> Transaction.Perk.PACKAGE_PERK
      TransactionEntity.Perk.UNKNOWN -> Transaction.Perk.UNKNOWN
    }
  }

  private fun map(perk: Transaction.Perk?): TransactionEntity.Perk? {
    if (perk == null) return null
    return when (perk) {
      Transaction.Perk.GAMIFICATION_LEVEL_UP -> TransactionEntity.Perk.GAMIFICATION_LEVEL_UP
      Transaction.Perk.PACKAGE_PERK -> TransactionEntity.Perk.PACKAGE_PERK
      Transaction.Perk.UNKNOWN -> TransactionEntity.Perk.UNKNOWN
    }
  }
}