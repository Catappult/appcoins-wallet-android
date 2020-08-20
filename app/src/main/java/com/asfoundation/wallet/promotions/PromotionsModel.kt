package com.asfoundation.wallet.promotions

data class PromotionsModel(val gamificationAvailable: Boolean,
                           val referralsAvailable: Boolean,
                           val promotions: List<Promotion>,
                           val maxBonus: Double)

