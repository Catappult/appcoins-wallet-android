package com.appcoins.wallet.gamification

import java.math.BigDecimal

data class LevelModel(val amount: BigDecimal, val bonus: Double, val level: Int,
                      val levelType: LevelType) {
  enum class LevelType {
    REACHED, CURRENT, UNREACHED
  }
}