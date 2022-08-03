package com.asfoundation.wallet.referrals

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

open class CardNotification(
  @StringRes open val title: Int? = null,
  @StringRes open val body: Int? = null,
  @DrawableRes open val icon: Int? = null,
  @StringRes open val positiveButtonText: Int? = null,
  open val positiveAction: CardNotificationAction,
  open val gamificationType: String? = null
)