package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class LevelsResponse(@SerializedName("result") val list: List<Level>,
                          val status: Status) {
  enum class Status {
    ACTIVE, INACTIVE
  }
}

data class Level(val amount: BigDecimal, val bonus: Double, val level: Int)
