package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class GenericResponse(
  id: String,
  priority: Int,
  gamificationStatus: GamificationStatus?,
  @SerializedName("current_progress") val currentProgress: BigDecimal?,
  @SerializedName("notification_description") val notificationDescription: String?,
  @SerializedName("perk_description") val perkDescription: String?,
  @SerializedName("app_name") val appName: String?,
  @SerializedName("end_date") val endDate: Long, val icon: String?,
  @SerializedName("linked_promotion_id") val linkedPromotionId: String?,
  @SerializedName("objective_progress") val objectiveProgress: BigDecimal?,
  @SerializedName("start_date") val startDate: Long?,
  @SerializedName("notification_title") val notificationTitle: String?,
  @SerializedName("view_type") val viewType: String,
  @SerializedName("details_link") val detailsLink: String?,
  @SerializedName("action_url") val actionUrl: String?,
  @SerializedName("package_name") val packageName: String?
) : PromotionsResponse(id, priority, gamificationStatus)