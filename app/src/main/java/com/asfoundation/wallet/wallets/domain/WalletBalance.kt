package com.asfoundation.wallet.wallets.domain

import com.asfoundation.wallet.ui.balance.TokenBalance
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue

data class WalletBalance(
  val overallFiat: FiatValue,
  val creditsOnlyFiat: FiatValue,
  val creditsBalance: TokenBalance,
  val appcBalance: TokenBalance,
  val ethBalance: TokenBalance
)