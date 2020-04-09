package com.asfoundation.wallet.ui.gamification

import java.math.BigDecimal

data class ViewLevel(val level: Int, val amount: BigDecimal, val bonus: Double,
                     val isCompleted: Boolean)

data class ViewLevels(
    val list: List<ViewLevel>,
    val level: Int,
    val status: Status,
    val updateDate: String?
)