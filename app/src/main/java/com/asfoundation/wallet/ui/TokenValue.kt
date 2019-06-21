package com.asfoundation.wallet.ui

import java.math.BigDecimal

data class TokenValue(val amount: BigDecimal, val currency: String, val symbol: String = "")
