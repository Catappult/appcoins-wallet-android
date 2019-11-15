package com.asfoundation.wallet.interact

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

data class UpdateNotification(@StringRes override val title: Int,
                              @StringRes override val body: Int, @StringRes
                              override val positiveButtonText: Int,
                              override val positiveAction: CardNotificationAction, @RawRes
                              val animation: Int) :
    CardNotification(title, body, null, positiveButtonText, positiveAction)
