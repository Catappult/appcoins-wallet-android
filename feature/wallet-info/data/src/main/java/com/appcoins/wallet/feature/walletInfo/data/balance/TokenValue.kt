package com.appcoins.wallet.feature.walletInfo.data.balance

import java.math.BigDecimal

data class TokenValue(val amount: BigDecimal, val currency: String, val symbol: String = "")
