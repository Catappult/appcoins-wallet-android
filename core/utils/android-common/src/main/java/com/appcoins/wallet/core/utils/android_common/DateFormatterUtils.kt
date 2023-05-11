package com.appcoins.wallet.core.utils.android_common

import android.text.format.DateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

object DateFormatterUtils {
  private const val SHORT_DATE_FORMAT = "MMM, dd yyyy"
  private const val LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSSX"
  fun getDate(timestamp: Long) =
    DateFormat.format(SHORT_DATE_FORMAT, timestamp).toString()

  fun getDate(time: String): String {
    val dateFormat = DateTimeFormatter.ofPattern(LONG_DATE_FORMAT)
    val instant = LocalDateTime.parse(time, dateFormat).atZone(ZoneOffset.UTC).toInstant()
    return DateFormat.format(SHORT_DATE_FORMAT, Date.from(instant)).toString()
  }
}