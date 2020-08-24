package com.appcoins.wallet.gamification.repository.entity


open class PromotionsResponse(
    val id: String,
    val priority: Int
) {
  enum class Status {
    ACTIVE, INACTIVE
  }
}