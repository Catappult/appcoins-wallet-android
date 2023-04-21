package com.appcoins.wallet.core.utils.android_common

import android.text.format.DateFormat

object DateFormatterUtils {
  fun getDate(timestamp: Long) =
    DateFormat.format("MMM, dd yyyy", timestamp).toString()
}