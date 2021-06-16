package com.asfoundation.wallet.ui.gamification

import java.math.BigDecimal

interface LevelItem {
  val amount: BigDecimal
  val bonus: Double
  val level: Int
}

data class ReachedLevelItem(override val amount: BigDecimal,
                            override val bonus: Double,
                            override val level: Int) : LevelItem

data class CurrentLevelItem(override val amount: BigDecimal,
                            override val bonus: Double,
                            override val level: Int,
                            val amountSpent: BigDecimal,
                            val nextLevelAmount: BigDecimal?) : LevelItem

data class UnreachedLevelItem(override val amount: BigDecimal,
                              override val bonus: Double,
                              override val level: Int) : LevelItem