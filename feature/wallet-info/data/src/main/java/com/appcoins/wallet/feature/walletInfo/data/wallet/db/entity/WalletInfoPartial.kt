package com.appcoins.wallet.feature.walletInfo.data.wallet.db.entity

import java.math.BigDecimal
import java.math.BigInteger

data class WalletInfoUpdate(
  val wallet: String,
  val appcCreditsBalanceWei: BigInteger,
  val appcBalanceWei: BigInteger,
  val ethBalanceWei: BigInteger,
  val blocked: Boolean,
  val verified: Boolean,
  val logging: Boolean,
  val hasBackup: Long
)

data class WalletInfoUpdateWithBalance(
  val wallet: String,
  val appcCreditsBalanceWei: BigInteger,
  val appcBalanceWei: BigInteger,
  val ethBalanceWei: BigInteger,
  val blocked: Boolean,
  val verified: Boolean,
  val logging: Boolean,
  val hasBackup: Long,
  val appcCreditsBalanceFiat: BigDecimal?,
  val appcBalanceFiat: BigDecimal?,
  val ethBalanceFiat: BigDecimal?,
  val fiatCurrency: String?,
  val fiatSymbol: String?,
)

data class WalletInfoUpdateName(val wallet: String, val name: String?)

data class WalletInfoDelete(val wallet: String)