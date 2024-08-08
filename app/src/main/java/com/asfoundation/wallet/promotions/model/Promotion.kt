package com.asfoundation.wallet.promotions.model

import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import java.math.BigDecimal

sealed class Promotion(open val id: String)

sealed class PerkPromotion(
  override val id: String,
  open val gamificationStatus: GamificationStatus?,
  open val startDate: Long?,
  open val endDate: Long,
) : Promotion(id)

data class DefaultItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  val description: String?,
  val icon: String?,
  val appName: String?,
  override val startDate: Long?,
  override val endDate: Long,
  val actionUrl: String?,
  val packageName: String?
) : PerkPromotion(id, gamificationStatus, startDate, endDate)

data class FutureItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  val description: String?,
  val icon: String?,
  val appName: String?,
  override val startDate: Long?,
  override val endDate: Long,
  val actionUrl: String?,
  val packageName: String?
) : PerkPromotion(id, gamificationStatus, startDate, endDate)

data class ProgressItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  override val startDate: Long?,
  override val endDate: Long,
) : PerkPromotion(id, gamificationStatus, startDate, endDate)

data class GamificationItem(
  override val id: String,
  val planet: Drawable?,
  val level: Int,
  val levelColor: Int,
  val toNextLevelAmount: BigDecimal?,
  val bonus: Double,
  val links: MutableList<GamificationLinkItem>
) : Promotion(id)

data class ReferralItem(
  override val id: String,
) : Promotion(id)

data class GamificationLinkItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  override val startDate: Long?,
  override val endDate: Long
) : PerkPromotion(id, gamificationStatus, startDate, endDate)

data class VoucherItem(
  override val id: String,
) : Promotion(id)

data class PromoCodeItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  val description: String?,
  val appName: String?,
  val icon: String?,
  override val startDate: Long?,
  override val endDate: Long,
  val actionUrl: String?,
  val packageName: String?
) : PerkPromotion(id, gamificationStatus, startDate, endDate)