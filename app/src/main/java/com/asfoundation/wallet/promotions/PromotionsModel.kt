package com.asfoundation.wallet.promotions

import java.math.BigDecimal

data class PromotionsModel(val gamificationAvailable: Boolean, val referralsAvailable: Boolean,
                           val level: Int, val nextLevelAmount: BigDecimal? = BigDecimal(-1),
                           val totalSpend: BigDecimal, val link: String? = "",
                           val maxValue: BigDecimal, val numberOfInvitations: Int,
                           val receivedValue: BigDecimal, val isValidated: Boolean,
                           val currency: String)

