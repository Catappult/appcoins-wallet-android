package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.iab.FiatValue

data class TokenBalance(val token: TokenValue, val fiat: FiatValue)