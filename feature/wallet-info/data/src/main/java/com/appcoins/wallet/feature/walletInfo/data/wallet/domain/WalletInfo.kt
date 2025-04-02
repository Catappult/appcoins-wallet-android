package com.appcoins.wallet.feature.walletInfo.data.wallet.domain

import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance

data class WalletInfo(
  val wallet: String,
  val name: String,
  val walletBalance: WalletBalance,
  val blocked: Boolean,
  val verified: Boolean,
  val logging: Boolean,
  val backupDate: Long,
  val canTransfer: Boolean
) {
  val hasBackup get() = backupDate > 0
}