package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

data class PromotionNotification(override val positiveAction: CardNotificationAction,
                                 val noResTitle: String?,
                                 val noResBody: String?,
                                 val noResIcon: String?,
                                 val id: String,
                                 val detailsLink: String?) :
    CardNotification(null, null, null, null, positiveAction)