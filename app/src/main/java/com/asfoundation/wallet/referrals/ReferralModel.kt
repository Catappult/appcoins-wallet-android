package com.asfoundation.wallet.referrals

import java.math.BigDecimal

data class ReferralModel(
    val completed: Int = 0, val link: String? = "", val invited: Boolean = false,
    val pendingAmount: BigDecimal = BigDecimal.ZERO,
    val amount: BigDecimal = BigDecimal.ZERO, val symbol: String = "",
    val maxAmount: BigDecimal = BigDecimal.ZERO,
    val minAmount: BigDecimal = BigDecimal.ZERO, val available: Int = 0,
    val receivedAmount: BigDecimal = BigDecimal.ZERO,
    val isRedeemed: Boolean = false, val isActive: Boolean = false)
