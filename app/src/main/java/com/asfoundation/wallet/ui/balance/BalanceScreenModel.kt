package com.asfoundation.wallet.ui.balance

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue

data class BalanceScreenModel(
  val overallFiat: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue,
  val creditsBalance: TokenBalance,
  val appcBalance: TokenBalance,
  val ethBalance: TokenBalance
)