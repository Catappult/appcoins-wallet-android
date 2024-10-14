package com.appcoins.wallet.core.utils.android_common

import android.text.format.DateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

object DateFormatterUtils {
  const val MONTH_DAY_YEAR_FORMAT = "MMM, dd yyyy"
  const val STANDARD_DATE_TIME_FORMAT = "MMM, dd yyyy, hh:mmaa"
  const val ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
  const val PRECISE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSSX"

  private fun formatter(pattern: String) = DateTimeFormatter.ofPattern(pattern)

  private fun getDate(time: String, format: String): String {
    val dateFormat = DateTimeFormatter.ofPattern(PRECISE_DATE_TIME_FORMAT)
    val instant = LocalDateTime.parse(time, dateFormat).atZone(ZoneOffset.UTC).toInstant()
    return DateFormat.format(format, Date.from(instant)).toString()
  }

  fun String.getDay(pattern: String = MONTH_DAY_YEAR_FORMAT) = getDate(this, pattern)

  fun String.getDayAndHour() = getDate(this, STANDARD_DATE_TIME_FORMAT)

  fun transformDate(date: String, fromPattern: String, toPattern: String): String =
    LocalDateTime.parse(date, formatter(fromPattern))
      .atZone(ZoneOffset.UTC)
      .format(formatter(toPattern))

  fun transformDateToTimestampSeconds(date: String, fromPattern: String): Long =
    LocalDateTime.parse(date, formatter(fromPattern)).atZone(ZoneOffset.UTC).toEpochSecond()
}
