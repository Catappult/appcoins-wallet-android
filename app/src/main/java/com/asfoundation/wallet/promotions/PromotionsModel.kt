package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.repository.entity.Status

data class PromotionsModel(val gamificationAvailable: Boolean,
                           val referralsAvailable: Boolean,
                           val promotions: List<Promotion>,
                           val maxBonus: Double,
                           val error: Status? = null)

