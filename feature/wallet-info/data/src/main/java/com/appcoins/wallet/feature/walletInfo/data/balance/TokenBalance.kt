package com.appcoins.wallet.feature.walletInfo.data.balance

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue

data class TokenBalance(val token: TokenValue, val fiat: FiatValue)