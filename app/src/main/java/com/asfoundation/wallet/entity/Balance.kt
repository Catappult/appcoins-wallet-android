package com.asfoundation.wallet.entity

data class Balance(val symbol: String, val value: String) {

  override fun toString(): String {
    return "$value $symbol"
  }
}
