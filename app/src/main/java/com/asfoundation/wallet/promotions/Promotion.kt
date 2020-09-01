package com.asfoundation.wallet.promotions

import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import java.math.BigDecimal

open class Promotion(val id: String)

class TitleItem(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val isGamificationTitle: Boolean,
    val bonus: String = "0.0",
    id: String = ""
) : Promotion(id)

class DefaultItem(
    id: String,
    val title: String,
    val icon: String?,
    val endDate: Long
) : Promotion(id)

class FutureItem(
    id: String,
    val title: String,
    val icon: String?,
    val endDate: Long
) : Promotion(id)

class ProgressItem(
    id: String,
    val title: String,
    val icon: String?,
    val endDate: Long,
    val current: BigDecimal,
    val objective: BigDecimal
) : Promotion(id)

class GamificationItem(
    id: String,
    val planet: Drawable?,
    val level: Int,
    val levelColor: Int,
    val title: String,
    val phrase: String,
    var bonus: Double,
    val links: MutableList<GamificationLinkItem>
) : Promotion(id)

class ReferralItem(
    id: String,
    val bonus: BigDecimal,
    val currency: String,
    val link: String
) : Promotion(id)

class GamificationLinkItem(
    val title: String,
    val icon: String?
)
