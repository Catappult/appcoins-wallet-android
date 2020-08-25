package com.appcoins.wallet.gamification.repository.entity

data class UserStatusResponse(val promotions: List<PromotionsResponse>, val error: Status? = null)

enum class Status {
  NO_NETWORK, UNKNOWN_ERROR
}
