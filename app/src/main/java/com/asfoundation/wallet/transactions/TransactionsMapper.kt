package com.asfoundation.wallet.transactions

import com.asfoundation.wallet.entity.WalletHistory
import com.asfoundation.wallet.transactions.TransactionDetails
import java.util.*

class TransactionsMapper {

  fun mapTransactionsFromWalletHistory(
      transactions: List<WalletHistory.Transaction>): List<Transaction> {

    val transactionList: MutableList<Transaction> = ArrayList(transactions.size)

    for (i in transactions.indices.reversed()) {
      val transaction = transactions[i]
      val txType = mapTransactionType(transaction)
      val status: Transaction.TransactionStatus = when (transaction.status) {
        WalletHistory.Status.SUCCESS -> Transaction.TransactionStatus.SUCCESS
        WalletHistory.Status.FAIL -> Transaction.TransactionStatus.FAILED
        else -> Transaction.TransactionStatus.FAILED
      }
      val sourceName = if (txType == Transaction.TransactionType.BONUS) {
        if (transaction.bonus == null) {
          null
        } else {
          transaction.bonus.stripTrailingZeros()
              .toPlainString()
        }
      } else {
        transaction.app
      }
      val bonusSubType = mapSubtype(transaction.subType)
      val perk = mapPerk(transaction.perk)
      transactionList.add(0,
          Transaction(transaction.txID, txType, bonusSubType, transaction.title,
              transaction.description, perk, null, transaction.ts.time,
              transaction.processedTime.time, status, transaction.amount.toString(),
              transaction.sender, transaction.receiver, TransactionDetails(sourceName,
              TransactionDetails.Icon(TransactionDetails.Icon.Type.URL, transaction.icon),
              transaction.sku),
              if (txType == Transaction.TransactionType.ETHER_TRANSFER) "ETH" else "APPC",
              mapOperations(transaction.operations)))
    }
    return transactionList
  }

  private fun mapPerk(perk: String?): Transaction.Perk? {
    var perkType: Transaction.Perk? = null
    if (perk != null) {
      perkType = when (perk) {
        GAMIFICATION_LEVEL_UP -> Transaction.Perk.GAMIFICATION_LEVEL_UP
        PACKAGE_PERK -> Transaction.Perk.PACKAGE_PERK
        else -> Transaction.Perk.UNKNOWN
      }
    }
    return perkType
  }

  private fun mapSubtype(subType: String?): Transaction.SubType? {
    var bonusSubType: Transaction.SubType? = null
    if (subType != null) {
      bonusSubType = if (subType == PERK_BONUS) {
        Transaction.SubType.PERK_PROMOTION
      } else {
        Transaction.SubType.UNKNOWN
      }
    }
    return bonusSubType
  }

  private fun mapOperations(operations: List<WalletHistory.Operation>): List<Operation> {
    val list: MutableList<Operation> =
        ArrayList(operations.size)
    for (operation in operations) {
      list.add(Operation(operation.transactionId,
          operation.sender,
          operation.receiver, operation.fee))
    }
    return list
  }

  private fun mapTransactionType(
      transaction: WalletHistory.Transaction): Transaction.TransactionType {
    return when (transaction.type) {
      "Transfer OffChain" -> Transaction.TransactionType.TRANSFER_OFF_CHAIN
      "Topup OffChain" -> Transaction.TransactionType.TOP_UP
      "IAP OffChain" -> Transaction.TransactionType.IAP_OFFCHAIN
      "bonus" -> Transaction.TransactionType.BONUS
      "PoA OffChain" -> Transaction.TransactionType.ADS_OFFCHAIN
      "Ether Transfer" -> Transaction.TransactionType.ETHER_TRANSFER
      "IAP" -> Transaction.TransactionType.IAP
      else -> Transaction.TransactionType.STANDARD
    }
  }

  companion object {
    private const val PERK_BONUS = "perk_bonus"
    private const val GAMIFICATION_LEVEL_UP = "GAMIFICATION_LEVEL_UP"
    private const val PACKAGE_PERK = "PACKAGE_PERK"
  }
}