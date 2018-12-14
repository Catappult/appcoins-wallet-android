package com.appcoins.wallet.gamification.repository

import java.math.BigDecimal

data class UserStats(val status: Status, val level: Int = -1,
                     val nextLevelAmount: BigDecimal? = BigDecimal.ZERO,
                     val bonus: Double = -1.0,
                     val totalSpend: BigDecimal = BigDecimal.ZERO,
                     val totalEarned: BigDecimal =  BigDecimal.ZERO,
                     val isActive: Boolean = false) {

  enum class Status {
    OK, NO_NETWORK, UNKNOWN_ERROR
  }
}
