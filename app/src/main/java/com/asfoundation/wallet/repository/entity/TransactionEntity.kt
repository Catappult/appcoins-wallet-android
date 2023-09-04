package com.asfoundation.wallet.repository.entity

import androidx.room.Embedded
import androidx.room.Entity

@Entity(primaryKeys = ["transactionId", "relatedWallet"])
data class TransactionEntity(val transactionId: String,
                             val relatedWallet: String,
                             val approveTransactionId: String?,
                             val perk: Perk?,
                             val type: TransactionType,
                             val method: Method?,
                             val subType: SubType?,
                             val title: String?,
                             val cardDescription: String?,
                             val timeStamp: Long,
                             val processedTime: Long,
                             val status: TransactionStatus,
                             val value: String,
                             val currency: String?,
                             val paidAmount: String?,
                             val paidCurrency: String?,
                             val from: String,
                             val to: String,
                             @Embedded val details: TransactionDetailsEntity?,
                             val operations: List<OperationEntity>?,
                             val orderReference: String?) {

  enum class Method {
    UNKNOWN, APPC, APPC_C, ETH
  }

  enum class TransactionType {
    STANDARD, IAP, ADS, IAP_OFFCHAIN, ADS_OFFCHAIN, BONUS, TOP_UP, TRANSFER_OFF_CHAIN, TRANSFER,
    ETHER_TRANSFER, BONUS_REVERT, TOP_UP_REVERT, IAP_REVERT, INAPP_SUBSCRIPTION, ESKILLS_REWARD,
    ESKILLS, CHALLENGE_REWARD,
  }

  enum class SubType {
    PERK_PROMOTION, UNKNOWN
  }

  enum class Perk {
    GAMIFICATION_LEVEL_UP, PACKAGE_PERK, UNKNOWN
  }

  enum class TransactionStatus {
    SUCCESS, FAILED, PENDING;
  }
}