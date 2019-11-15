package com.asfoundation.wallet.referrals

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

open class CardNotification(@StringRes open val title: Int,
                            @StringRes open val body: Int,
                            @DrawableRes open val icon: Int?, @StringRes
                            open val positiveButtonText: Int,
                            open val positiveAction: CardNotificationAction)