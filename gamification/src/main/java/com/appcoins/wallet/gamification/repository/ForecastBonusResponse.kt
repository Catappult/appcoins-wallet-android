package com.appcoins.wallet.gamification.repository

import java.math.BigDecimal

data class ForecastBonusResponse(val bonus: BigDecimal, val level: Int, val status: Status?) {
  enum class Status {
    ACTIVE, INACTIVE
  }
}
