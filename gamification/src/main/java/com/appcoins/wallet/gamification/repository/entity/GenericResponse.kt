package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class GenericResponse(
    id: String,
    priority: Int,
    @SerializedName("current_progress")
    val currentProgress: BigDecimal?,
    val description: String,
    @SerializedName("end_date")
    val endDate: Long,
    val icon: String?,
    @SerializedName("linked_promotion_id")
    val linkedPromotionId: String?,
    @SerializedName("objective_progress")
    val objectiveProgress: BigDecimal?,
    @SerializedName("start_date")
    val startDate: Long?,
    val title: String,
    @SerializedName("view_type") val viewType: String,
    @SerializedName("details_link") val detailsLink: String?
) : PromotionsResponse(id, priority)