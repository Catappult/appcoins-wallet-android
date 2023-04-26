package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.ui.TokenValue
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue

data class TokenBalance(val token: TokenValue, val fiat: com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue)