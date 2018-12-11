package com.asfoundation.wallet.ui.gamification

import java.math.BigDecimal

data class ViewLevel(val level: Int, val amount: BigDecimal, val bonus: Double,
                     val isCompleted: Boolean)
