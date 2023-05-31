package com.appcoins.wallet.feature.changecurrency.data.currencies

import java.io.Serializable
import java.math.BigDecimal

data class FiatValue(val amount: BigDecimal, val currency: String, val symbol: String = "") :
    Serializable {

  constructor() : this(BigDecimal.ZERO, "", "")

  override fun equals(other: Any?) = other is FiatValue
      && other.amount.compareTo(this.amount) == 0
      && other.currency == this.currency
      && other.symbol == this.symbol

  override fun hashCode(): Int {
    var result = amount.hashCode()
    result = 31 * result + currency.hashCode()
    result = 31 * result + symbol.hashCode()
    return result
  }
}
