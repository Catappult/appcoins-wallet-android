package com.appcoins.wallet.feature.walletInfo.data.wallet.domain

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.walletInfo.data.balance.TokenBalance
import com.appcoins.wallet.feature.walletInfo.data.balance.TokenValue
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import java.math.BigDecimal

data class WalletInfo(
  val wallet: String,
  val name: String,
  val walletBalance: WalletBalance,
  val blocked: Boolean,
  val verified: Boolean,
  val logging: Boolean,
  val backupDate: Long
) {
  val hasBackup get() = backupDate > 0
}

val emptyWalletInfo = WalletInfo(
  wallet = "walletAddress",
  name = "Name",
  walletBalance = WalletBalance(
    overallFiat = FiatValue(),
    creditsOnlyFiat = FiatValue(),
    creditsBalance = TokenBalance(
      token = TokenValue(
        amount = BigDecimal(50.0),
        currency = "EUR",
        symbol = "€"
      ),
      fiat = FiatValue(
        amount = BigDecimal(50.0),
        currency = "EUR",
        symbol = "€"
      )
    ),
    appcBalance = TokenBalance(
      token = TokenValue(
        amount = BigDecimal(50.0),
        currency = "EUR",
        symbol = "€"
      ),
      fiat = FiatValue(
        amount = BigDecimal(50.0),
        currency = "EUR",
        symbol = "€"
      )
    ),
    ethBalance = TokenBalance(
      token = TokenValue(
        amount = BigDecimal(50.0),
        currency = "EUR",
        symbol = "€"
      ),
      fiat = FiatValue(
        amount = BigDecimal(50.0),
        currency = "EUR",
        symbol = "€"
      )
    )
  ),
  blocked = false,
  verified = true,
  logging = false,
  backupDate = 0L
)
