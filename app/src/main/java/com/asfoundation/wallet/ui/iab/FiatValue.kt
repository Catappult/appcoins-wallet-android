package com.asfoundation.wallet.ui.iab

import java.io.Serializable
import java.math.BigDecimal

class FiatValue(val amount: BigDecimal, val currency: String, val symbol: String = "") : Serializable
