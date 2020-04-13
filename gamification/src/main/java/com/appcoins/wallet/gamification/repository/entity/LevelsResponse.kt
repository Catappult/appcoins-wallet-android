package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.*

data class LevelsResponse(@SerializedName("result") val list: List<Level>,
                          val status: Status,
                          @SerializedName("update_date") val updateDate: Date?) {
  enum class Status {
    ACTIVE, INACTIVE
  }
}

data class Level(val amount: BigDecimal, val bonus: Double, val level: Int)
