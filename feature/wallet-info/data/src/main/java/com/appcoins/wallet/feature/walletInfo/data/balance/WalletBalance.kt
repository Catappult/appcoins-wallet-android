package com.appcoins.wallet.feature.walletInfo.data.balance

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.google.gson.annotations.SerializedName

data class WalletBalance(
  val overallFiat: FiatValue,
  val creditsOnlyFiat: FiatValue,
  val creditsBalance: TokenBalance,
  val appcBalance: TokenBalance,
  val ethBalance: TokenBalance
) : java.io.Serializable