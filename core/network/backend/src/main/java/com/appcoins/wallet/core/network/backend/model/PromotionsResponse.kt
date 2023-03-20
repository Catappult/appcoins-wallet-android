package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName


open class PromotionsResponse(
  val id: String,
  val priority: Int,
  @SerializedName("gamification_status") val gamificationStatus: GamificationStatus?,
) {
  enum class Status {
    ACTIVE, INACTIVE
  }
}