package com.appcoins.wallet.core.network.backend.model

import java.math.BigDecimal

data class ForecastBonusResponse(val bonus: BigDecimal, val level: Int, val status: Status?) {
  @Suppress("unused")
  enum class Status {
    ACTIVE, INACTIVE
  }
}
