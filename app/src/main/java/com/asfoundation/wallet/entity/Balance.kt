package com.asfoundation.wallet.entity

import java.math.BigDecimal

data class Balance(val symbol: String, val value: BigDecimal) {

  fun getStringValue(): String {
    return value
        .stripTrailingZeros()
        .toPlainString()
  }

  override fun toString(): String {
    return "$value $symbol"
  }
}
