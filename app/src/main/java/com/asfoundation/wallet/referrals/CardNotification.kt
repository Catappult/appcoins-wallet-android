package com.asfoundation.wallet.referrals

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

open class CardNotification(open val id: Int, @StringRes open val title: Int,
                            @StringRes open val body: Int,
                            @DrawableRes open val icon: Int?)