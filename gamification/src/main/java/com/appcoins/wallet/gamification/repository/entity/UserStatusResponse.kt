package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class UserStatusResponse(val bonus: Double,
                              @SerializedName("current_amount")
                              val totalSpend: BigDecimal,
                              val level: Int,
                              @SerializedName("next_level_amount") val nextLevelAmount: BigDecimal)
