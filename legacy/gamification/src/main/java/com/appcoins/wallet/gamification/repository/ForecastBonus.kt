package com.appcoins.wallet.gamification.repository

import java.math.BigDecimal

data class ForecastBonus(
    val status: Status,
    val amount: BigDecimal = BigDecimal.ZERO,
    val level: Int = 0,
    val currency: String = "") {

  enum class Status {
    ACTIVE, INACTIVE, NO_NETWORK, UNKNOWN_ERROR
  }
}
