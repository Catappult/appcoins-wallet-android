package com.asfoundation.wallet.promotions

import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

data class PromotionNotification(
  override val positiveAction: CardNotificationAction,
  override val gamificationType: String?
) :
  CardNotification(null, null, null, null, positiveAction, gamificationType)