package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.*

data class LevelsResponse(@SerializedName("result") val list: List<Level>,
                          val status: Status,
                          @SerializedName("update_date") val updateDate: Date?) {

  @Suppress("unused")
  enum class Status {
    ACTIVE, INACTIVE
  }
}

data class Level(val amount: BigDecimal, val bonus: Double, val level: Int)
