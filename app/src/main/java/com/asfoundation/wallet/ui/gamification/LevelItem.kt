package com.asfoundation.wallet.ui.gamification

import java.math.BigDecimal

interface LevelItem {
  val amount: BigDecimal
  val bonus: Double
  val level: Int
  val currency: String?
}

data class ReachedLevelItem(
  override val amount: BigDecimal,
  override val bonus: Double,
  override val level: Int,
  override val currency: String?,
) : LevelItem

data class CurrentLevelItem(
  override val amount: BigDecimal,
  override val bonus: Double,
  override val level: Int,
  val amountSpent: BigDecimal,
  val nextLevelAmount: BigDecimal?,
  override val currency: String?,
) : LevelItem

data class UnreachedLevelItem(
  override val amount: BigDecimal,
  override val bonus: Double,
  override val level: Int,
  override val currency: String?,
) : LevelItem