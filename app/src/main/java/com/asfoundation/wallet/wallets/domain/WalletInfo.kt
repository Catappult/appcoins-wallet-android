package com.asfoundation.wallet.wallets.domain

data class WalletInfo(
  val wallet: String,
  val walletBalance: WalletBalance,
  val blocked: Boolean,
  val verified: Boolean,
  val logging: Boolean,
)