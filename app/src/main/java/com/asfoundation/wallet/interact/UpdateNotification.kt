package com.asfoundation.wallet.interact

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.asfoundation.wallet.referrals.CardNotification

data class UpdateNotification(override val id: Int, @StringRes override val title: Int,
                              @StringRes override val body: Int, @RawRes val animation: Int) :
    CardNotification(id, title, body, null)
