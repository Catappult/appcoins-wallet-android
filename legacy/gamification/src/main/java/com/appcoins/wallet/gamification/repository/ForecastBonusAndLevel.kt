package com.appcoins.wallet.gamification.repository

import java.io.Serializable
import java.math.BigDecimal

data class ForecastBonusAndLevel(
    val status: ForecastBonus.Status,
    val amount: BigDecimal = BigDecimal.ZERO,
    val currency: String = "",
    val minAmount: BigDecimal = BigDecimal.ZERO,
    val level: Int = 0) : Serializable
