package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.repository.entity.GamificationResponse
import java.math.BigDecimal

data class PromotionsModel(val showGamification: Boolean, val showReferrals: Boolean,
                           val level: Int, val nextLevelAmount: BigDecimal? = BigDecimal(-1),
                           val totalSpend: BigDecimal, val status: GamificationResponse.Status,
                           val link: String? = "", val maxValue: BigDecimal,
                           val numberOfInvitations: Int, val receivedValue: BigDecimal,
                           val isValidated: Boolean, val currency: String)

