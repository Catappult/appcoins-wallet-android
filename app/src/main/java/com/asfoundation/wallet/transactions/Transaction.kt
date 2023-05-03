package com.asfoundation.wallet.transactions

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    val transactionId: String,
    val type: TransactionType,
    val subType: SubType?,
    val method: Method,
    val title: String?,
    val description: String?,
    val perk: Perk?,
    val approveTransactionId: String?,
    val timeStamp: Long,
    val processedTime: Long,
    val status: TransactionStatus,
    val value: String,
    val from: String,
    val to: String,
    val details: TransactionDetails?,
    val currency: String?,
    val operations: List<Operation>?,
    val linkedTx: List<Transaction>?,
    val paidAmount: String?,
    val paidCurrency: String?,
    val orderReference: String?
) : Parcelable {

  @Parcelize
  enum class TransactionType : Parcelable {
    STANDARD, IAP, ADS, IAP_OFFCHAIN, ADS_OFFCHAIN, BONUS, TOP_UP, TRANSFER_OFF_CHAIN,
    BONUS_REVERT, TOP_UP_REVERT, IAP_REVERT, SUBS_OFFCHAIN, ESKILLS_REWARD, ESKILLS, TRANSFER,
    ETHER_TRANSFER
  }

  @Parcelize
  enum class Method : Parcelable { UNKNOWN, APPC, APPC_C, ETH }

  @Parcelize
  enum class SubType : Parcelable { PERK_PROMOTION, UNKNOWN }

  @Parcelize
  enum class Perk : Parcelable { GAMIFICATION_LEVEL_UP, PACKAGE_PERK, UNKNOWN }

  @Parcelize
  enum class TransactionStatus : Parcelable { SUCCESS, FAILED, PENDING }

  data class TransactionCardInfo(
    val title: Int,
    val icon: Int? = null,
    val appIcon: String? = null,
    val description: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val subIcon: Int? = null
  )
}