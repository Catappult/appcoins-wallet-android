package com.appcoins.wallet.gamification.repository

import java.math.BigDecimal

data class ForecastBonus(
    val status: Status,
    val amount: BigDecimal = BigDecimal.ZERO) {

  enum class Status {
    OK, NO_NETWORK, UNKNOWN_ERROR
  }
}
