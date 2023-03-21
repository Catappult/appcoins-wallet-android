package com.appcoins.wallet.core.network.microservices.model

/** Class copied from com.asfoundation.wallet.repository.entity TOPUP not TOP_UP */
enum class TransactionType {
  STANDARD, IAP, ADS, IAP_OFFCHAIN, ADS_OFFCHAIN, BONUS, TOPUP, TRANSFER_OFF_CHAIN, TRANSFER,
  ETHER_TRANSFER, BONUS_REVERT, TOP_UP_REVERT, IAP_REVERT, INAPP_SUBSCRIPTION, ESKILLS_REWARD,
  ESKILLS;
}