package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import java.math.BigDecimal

data class PromotionsGamificationStats(
  val resultState: ResultState,
  val level: Int = INVALID_LEVEL,
  val nextLevelAmount: BigDecimal? = BigDecimal.ZERO,
  val bonus: Double = INVALID_BONUS,
  val totalSpend: BigDecimal = BigDecimal.ZERO,
  val totalEarned: BigDecimal = BigDecimal.ZERO,
  val isActive: Boolean = false,
  val fromCache: Boolean = false,
  val gamificationStatus: GamificationStatus
) {

  enum class ResultState {
    OK,
    NO_NETWORK,
    UNKNOWN_ERROR
  }

  companion object {
    const val INVALID_LEVEL = -1
    const val INVALID_BONUS = -1.0
  }
}
