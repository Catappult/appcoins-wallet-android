package com.asfoundation.wallet.ui.gamification

import java.math.BigDecimal

data class UserRewardsStatus(val level: Int = 0, val receivedAmount: BigDecimal = BigDecimal.ZERO,
                             val bonus: List<Double> = mutableListOf())
