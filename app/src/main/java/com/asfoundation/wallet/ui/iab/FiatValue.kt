package com.asfoundation.wallet.ui.iab

import java.io.Serializable
import java.math.BigDecimal

data class FiatValue(val amount: BigDecimal, val currency: String, val symbol: String = "") :
    Serializable {

  override fun equals(other: Any?) = other is FiatValue
        && other.amount.compareTo(this.amount) == 0
        && other.currency == this.currency
        && other.symbol == this.symbol
}
