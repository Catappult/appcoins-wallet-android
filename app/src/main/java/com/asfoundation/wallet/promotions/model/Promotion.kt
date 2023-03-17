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
  open val detailsLink: String?
) : Promotion(id)

data class TitleItem(
  @StringRes val title: Int,
  @StringRes val subtitle: Int,
  val isGamificationTitle: Boolean,
  val bonus: String = "0.0",
  override val id: String = ""
) : Promotion(id)

data class DefaultItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  val description: String?,
  val icon: String?,
  val appName: String?,
  override val startDate: Long?,
  override val endDate: Long,
  override val detailsLink: String?
) : PerkPromotion(id, gamificationStatus, startDate, endDate, detailsLink)

data class FutureItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  val description: String?,
  val icon: String?,
  val appName: String?,
  override val startDate: Long?,
  override val endDate: Long,
  override val detailsLink: String?
) : PerkPromotion(id, gamificationStatus, startDate, endDate, detailsLink)

data class ProgressItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  val description: String?,
  val appName: String?,
  val icon: String?,
  override val startDate: Long?,
  override val endDate: Long,
  val current: BigDecimal,
  val objective: BigDecimal?,
  override val detailsLink: String?
) : PerkPromotion(id, gamificationStatus, startDate, endDate, detailsLink)

data class GamificationItem(
  override val id: String,
  val planet: Drawable?,
  val level: Int,
  val gamificationStatus: GamificationStatus?,
  val levelColor: Int,
  val title: String,
  val toNextLevelAmount: BigDecimal?,
  val bonus: Double,
  val links: MutableList<GamificationLinkItem>
) : Promotion(id)

data class ReferralItem(
  override val id: String,
  val bonus: BigDecimal,
  val currency: String,
  val link: String
) : Promotion(id)

data class GamificationLinkItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  val description: String?,
  val icon: String?,
  override val startDate: Long?,
  override val endDate: Long
) : PerkPromotion(id, gamificationStatus, startDate, endDate, null)

data class VoucherItem(
  override val id: String,
  val packageName: String,
  val title: String,
  val icon: String,
  val hasAppcoins: Boolean,
  val maxBonus: Double
) : Promotion(id)

data class PromoCodeItem(
  override val id: String,
  override val gamificationStatus: GamificationStatus?,
  val description: String?,
  val appName: String?,
  val icon: String?,
  override val startDate: Long?,
  override val endDate: Long
) : PerkPromotion(id, gamificationStatus, startDate, endDate, null)