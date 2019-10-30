package com.asfoundation.wallet.referrals

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.math.BigDecimal

data class ReferralNotification(override val id: Int, @StringRes override val title: Int,
                                @StringRes override val body: Int,
                                @DrawableRes override val icon: Int,
                                val pendingAmount: BigDecimal, val symbol: String) :
    CardNotification(id, title, body, icon)