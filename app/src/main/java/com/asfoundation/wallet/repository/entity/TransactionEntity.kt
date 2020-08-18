package com.asfoundation.wallet.repository.entity

import androidx.room.Embedded
import androidx.room.Entity

@Entity(primaryKeys = ["transactionId", "relatedWallet"])
data class TransactionEntity(val transactionId: String,
                             val relatedWallet: String,
                             val approveTransactionId: String?,
                             val type: TransactionType,
                             val subType: SubType?,
                             val title: String?,
                             val cardDescription: String?,
                             val timeStamp: Long,
                             val processedTime: Long,
                             val status: TransactionStatus,
                             val value: String,
                             val from: String,
                             val to: String,
                             @Embedded val details: TransactionDetailsEntity?,
                             val currency: String?,
                             val operations: List<OperationEntity>?) {

  enum class TransactionType {
    STANDARD, IAP, ADS, IAP_OFFCHAIN, ADS_OFFCHAIN, BONUS, TOP_UP, TRANSFER_OFF_CHAIN,
    ETHER_TRANSFER;
  }

  enum class SubType {
    PROMOTIONS, UNKNOWN
  }

  enum class TransactionStatus {
    SUCCESS, FAILED, PENDING;
  }
}