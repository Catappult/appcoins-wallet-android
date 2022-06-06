package com.asfoundation.wallet.my_wallets.more

data class MoreDialogData(
  val walletAddress: String,
  val totalFiatBalance: String,
  val appcoinsBalance: String,
  val creditsBalance: String,
  val ethereumBalance: String,
  val showDeleteWallet: Boolean
)