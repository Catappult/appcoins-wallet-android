package com.asfoundation.wallet.ui.balance

data class BalanceScreenModel(
    val overallFiat: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue,
    val creditsBalance: com.appcoins.wallet.feature.walletInfo.data.domain.TokenBalance,
    val appcBalance: com.appcoins.wallet.feature.walletInfo.data.domain.TokenBalance,
    val ethBalance: com.appcoins.wallet.feature.walletInfo.data.domain.TokenBalance
)