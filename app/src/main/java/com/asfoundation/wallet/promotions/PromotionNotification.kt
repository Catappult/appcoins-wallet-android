package com.asfoundation.wallet.promotions

import androidx.annotation.StringRes
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

data class PromotionNotification(@StringRes override val positiveButtonText: Int,
                                 override val positiveAction: CardNotificationAction,
                                 val noResTitle: String?,
                                 val noResBody: String?,
                                 val noResIcon: String?) :
    CardNotification(null, null, null, positiveButtonText, positiveAction)