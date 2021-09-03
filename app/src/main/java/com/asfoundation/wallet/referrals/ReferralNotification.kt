package com.asfoundation.wallet.referrals

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import java.math.BigDecimal

data class ReferralNotification(@StringRes override val title: Int,
                                @StringRes override val body: Int,
                                @DrawableRes override val icon: Int, @StringRes
                                override val positiveButtonText: Int,
                                override val positiveAction: CardNotificationAction,
                                val pendingAmount: BigDecimal, val symbol: String) :
    CardNotification(title, body, icon, positiveButtonText, positiveAction)