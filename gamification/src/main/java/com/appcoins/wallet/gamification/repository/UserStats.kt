package com.appcoins.wallet.gamification.repository

import java.math.BigDecimal

data class UserStats(val status: Status, val level: Int,
                     val nextLevelAmount: BigDecimal?,
                     val bonus: Double,
                     val totalSpend: BigDecimal,
                     val totalEarned: BigDecimal) {
  constructor(status: Status) : this(status, -1, BigDecimal.ZERO, -1.0, BigDecimal.ZERO, BigDecimal.ZERO)


  enum class Status {
    OK, NO_NETWORK, UNKNOWN_ERROR
  }
}
