package com.appcoins.wallet.core.network.backend.model

import java.math.BigDecimal

data class ForecastBonusResponse(val level: BigDecimal, val bonus: BigDecimal, val status: Status?) {
  @Suppress("unused")
  enum class Status {
    ACTIVE, INACTIVE
  }
}
