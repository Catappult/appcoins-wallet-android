package com.asfoundation.wallet.referrals

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.math.BigDecimal

data class ReferralNotification(
    val id: Int,
    @StringRes val title: Int,
    @StringRes val body: Int,
    @DrawableRes val icon: Int,
    val pendingAmount: BigDecimal,
    val symbol: String
)