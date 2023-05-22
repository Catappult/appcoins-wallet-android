package com.appcoins.wallet.gamification.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appcoins.wallet.core.network.backend.model.PromotionsResponse
import com.appcoins.wallet.core.network.backend.model.ReferralResponse
import java.math.BigDecimal

// a uid field needed to be created because the previous primary key ('id') was not necessarily
// unique for all kinds of promotions.
@Entity
data class PromotionEntity(
  @PrimaryKey(autoGenerate = true) val uid: Long = 0,
  val id: String,
  val priority: Int,
  @ColumnInfo(name = "gamification_type")
  val gamificationStatus: String?,
  val bonus: Double? = null,
  @ColumnInfo(name = "total_spend")
  val totalSpend: BigDecimal? = null,
  @ColumnInfo(name = "total_earned")
  val totalEarned: BigDecimal? = null,
  val level: Int? = null,
  @ColumnInfo(name = "next_level_amount")
  val nextLevelAmount: BigDecimal? = null,
  val status: PromotionsResponse.Status? = null,
  @ColumnInfo(name = "max_amount")
  val maxAmount: BigDecimal? = null,
  val available: Int? = null,
  val bundle: Boolean? = null,
  val completed: Int? = null,
  val currency: String? = null,
  val symbol: String? = null,
  val invited: Boolean? = null,
  val link: String? = null,
  @ColumnInfo(name = "pending_amount")
  val pendingAmount: BigDecimal? = null,
  @ColumnInfo(name = "received_amount")
  val receivedAmount: BigDecimal? = null,
  @ColumnInfo(name = "user_status")
  val userStatus: ReferralResponse.UserStatus? = null,
  @ColumnInfo(name = "min_amount")
  val minAmount: BigDecimal? = null,
  val amount: BigDecimal? = null,
  @ColumnInfo(name = "current_progress")
  val currentProgress: BigDecimal? = null,
  @ColumnInfo(name = "notification_description")
  val notificationDescription: String? = null,
  @ColumnInfo(name = "perk_description")
  val perkDescription: String? = null,
  @ColumnInfo(name = "app_name")
  val appName: String? = null,
  @ColumnInfo(name = "end_date")
  val endDate: Long? = null,
  val icon: String? = null,
  @ColumnInfo(name = "linked_promotion_id")
  val linkedPromotionId: String? = null,
  @ColumnInfo(name = "objective_progress")
  val objectiveProgress: BigDecimal? = null,
  @ColumnInfo(name = "start_date")
  val startDate: Long? = null,
  @ColumnInfo(name = "notification_title")
  val notificationTitle: String? = null,
  @ColumnInfo(name = "view_type")
  val viewType: String? = null,
  @ColumnInfo(name = "details_link")
  val detailsLink: String? = null
)