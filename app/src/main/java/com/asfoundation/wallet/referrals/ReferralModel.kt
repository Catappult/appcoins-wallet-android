package com.asfoundation.wallet.referrals

import java.math.BigDecimal

data class ReferralModel(
    val completed: Int, val link: String?, val invited: Boolean, val pendingAmount: BigDecimal,
    val amount: BigDecimal, val symbol: String, val maxAmount: BigDecimal,
    val minAmount: BigDecimal, val available: Int, val receivedAmount: BigDecimal,
    val isRedeemed: Boolean, val isActive: Boolean)
