package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class GamificationResponse(
  id: String,
  priority: Int,
  gamificationStatus: GamificationStatus?,
  val bonus: Double,
  @SerializedName("current_amount") val totalSpend: BigDecimal,
  @SerializedName("bonus_earned") val totalEarned: BigDecimal,
  val level: Int,
  @SerializedName("next_level_amount") val nextLevelAmount: BigDecimal?,
  val status: Status,
  val bundle: Boolean
) : PromotionsResponse(id, priority, gamificationStatus)