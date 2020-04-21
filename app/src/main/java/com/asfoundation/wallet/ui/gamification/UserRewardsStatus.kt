package com.asfoundation.wallet.ui.gamification

import java.math.BigDecimal

data class UserRewardsStatus(val lastShownLevel: Int = 0, val level: Int = 0,
                             val toNextLevelAmount: BigDecimal = BigDecimal.ZERO,
                             val bonus: List<Double> = mutableListOf(),
                             val status: Status, val maxBonus: String = "0.0",
                             val pioneer: Boolean = false)//TODO Change after backend ready

enum class Status {
  OK, NO_NETWORK, UNKNOWN_ERROR
}
