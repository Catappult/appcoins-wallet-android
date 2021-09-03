package com.asfoundation.wallet.backup

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction

data class BackupNotification(@StringRes override val title: Int,
                              @StringRes override val body: Int,
                              @DrawableRes override val icon: Int, @StringRes
                              override val positiveButtonText: Int,
                              override val positiveAction: CardNotificationAction) :
    CardNotification(title, body, icon, positiveButtonText, positiveAction)