package com.asfoundation.wallet.transactions

import com.appcoins.wallet.core.network.backend.model.WalletHistory
import com.asfoundation.wallet.repository.entity.OperationEntity
import com.asfoundation.wallet.repository.entity.TransactionDetailsEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity
import java.util.*
import javax.inject.Inject

class TransactionsMapper @Inject constructor() {

  companion object {
    private const val PERK_BONUS = "perk_bonus"
    private const val GAMIFICATION_LEVEL_UP = "GAMIFICATION_LEVEL_UP"
    private const val PACKAGE_PERK = "PACKAGE_PERK"
  }

  fun map(transaction: WalletHistory.Transaction, wallet: String): TransactionEntity {
    val txType = mapTransactionType(transaction)
    val method = mapTransactionMethod(transaction.method)
    val status = map(transaction.status)
    val sourceName = mapSource(txType, transaction)
    val bonusSubType = mapSubtype(transaction.subType)
    val perk = mapPerk(transaction.perk)
    val operations = mapOperations(transaction.operations)
    val icon = TransactionDetailsEntity.Icon(TransactionDetailsEntity.Type.URL, transaction.icon)
    val details = TransactionDetailsEntity(icon, sourceName, transaction.sku)
    val currency = if (txType == TransactionEntity.TransactionType.ETHER_TRANSFER) "ETH" else "APPC"
    return TransactionEntity(transaction.txID, wallet, null, perk, txType, method, bonusSubType,
        transaction.title, transaction.description, transaction.ts.time,
        transaction.processedTime.time, status, transaction.amount.toString(), currency,
        transaction.paidAmount, transaction.paidCurrency, transaction.sender,
        transaction.receiver, details, operations, transaction.orderReference)
  }

  private fun mapSource(txType: TransactionEntity.TransactionType,
                        transaction: WalletHistory.Transaction): String? {
    return if (txType == TransactionEntity.TransactionType.BONUS) {
      if (transaction.bonus == null) {
        null
      } else {
        transaction.bonus.stripTrailingZeros()
            .toPlainString()
      }
    } else {
      transaction.app
    }
  }

  private fun map(status: WalletHistory.Status): TransactionEntity.TransactionStatus {
    return when (status) {
      WalletHistory.Status.SUCCESS -> TransactionEntity.TransactionStatus.SUCCESS
      WalletHistory.Status.FAIL -> TransactionEntity.TransactionStatus.FAILED
    }
  }

  fun isRevertType(type: String): Boolean {
    return when (type) {
      "Bonus Revert OffChain",
      "Topup Revert OffChain",
      "IAP Revert OffChain" -> true
      else -> false
    }
  }

  private fun mapTransactionType(
      transaction: WalletHistory.Transaction): TransactionEntity.TransactionType {
    return when (transaction.type) {
      "Transfer" -> TransactionEntity.TransactionType.TRANSFER
      "Transfer OffChain" -> TransactionEntity.TransactionType.TRANSFER_OFF_CHAIN
      "Ether Transfer" -> TransactionEntity.TransactionType.ETHER_TRANSFER
      "Topup OffChain" -> TransactionEntity.TransactionType.TOP_UP
      "IAP OffChain" -> TransactionEntity.TransactionType.IAP_OFFCHAIN
      "bonus" -> TransactionEntity.TransactionType.BONUS
      "PoA OffChain" -> TransactionEntity.TransactionType.ADS_OFFCHAIN
      "IAP" -> TransactionEntity.TransactionType.IAP
      "Bonus Revert OffChain" -> TransactionEntity.TransactionType.BONUS_REVERT
      "Topup Revert OffChain" -> TransactionEntity.TransactionType.TOP_UP_REVERT
      "IAP Revert OffChain" -> TransactionEntity.TransactionType.IAP_REVERT
      "Subscription OffChain" -> TransactionEntity.TransactionType.INAPP_SUBSCRIPTION
      "Eskills Reward OffChain" -> TransactionEntity.TransactionType.ESKILLS_REWARD
      "Eskills OffChain" -> TransactionEntity.TransactionType.ESKILLS
      else -> TransactionEntity.TransactionType.STANDARD
    }
  }

  private fun mapTransactionMethod(method: String?): TransactionEntity.Method {
    return when (method) {
      "appcoins_credits" -> TransactionEntity.Method.APPC_C
      "appcoins" -> TransactionEntity.Method.APPC
      "ETH" -> TransactionEntity.Method.ETH
      else -> TransactionEntity.Method.UNKNOWN
    }
  }

  private fun mapPerk(perk: String?): TransactionEntity.Perk? {
    return perk?.let {
      when (it) {
        GAMIFICATION_LEVEL_UP -> TransactionEntity.Perk.GAMIFICATION_LEVEL_UP
        PACKAGE_PERK -> TransactionEntity.Perk.PACKAGE_PERK
        else -> TransactionEntity.Perk.UNKNOWN
      }
    }
  }

  private fun mapSubtype(subType: String?): TransactionEntity.SubType? {
    var bonusSubType: TransactionEntity.SubType? = null
    if (subType != null) {
      bonusSubType = if (subType == PERK_BONUS) {
        TransactionEntity.SubType.PERK_PROMOTION
      } else {
        TransactionEntity.SubType.UNKNOWN
      }
    }
    return bonusSubType
  }

  private fun mapOperations(operations: List<WalletHistory.Operation>): List<OperationEntity> {
    val list: MutableList<OperationEntity> = ArrayList(operations.size)
    for (operation in operations) {
      list.add(OperationEntity(operation.transactionId,
          operation.sender,
          operation.receiver, operation.fee))
    }
    return list
  }

}