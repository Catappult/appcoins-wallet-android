package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.ui.iab.FiatValue

data class BalanceScreenModel(
    val overallFiat: FiatValue,
    val creditsBalance: TokenBalance,
    val appcBalance: TokenBalance,
    val ethBalance: TokenBalance
)