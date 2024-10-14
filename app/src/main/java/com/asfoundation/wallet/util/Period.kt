package com.asfoundation.wallet.util

import android.content.Context
import com.asf.wallet.R
import java.io.Serializable
import java.time.format.DateTimeParseException
import java.util.Objects
import java.util.regex.Matcher
import java.util.regex.Pattern


data class Period(val years: Int, val months: Int, val weeks: Int, val days: Int) : Serializable {

  companion object {
    /**
     * The pattern for parsing.
     */
    private val PATTERN = Pattern.compile(
      "([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?",
      Pattern.CASE_INSENSITIVE
    )


    //-----------------------------------------------------------------------
    /**
     * Obtains a `Period` from a text string such as `PnYnMnD`.
     *
     *
     * This will parse the string produced by `toString()` which is
     * based on the ISO-8601 period formats `PnYnMnD` and `PnW`.
     *
     *
     * The string starts with an optional sign, denoted by the ASCII negative
     * or positive symbol. If negative, the whole period is negated.
     * The ASCII letter "P" is next in upper or lower case.
     * There are then four sections, each consisting of a number and a suffix.
     * At least one of the four sections must be present.
     * The sections have suffixes in ASCII of "Y", "M", "W" and "D" for
     * years, months, weeks and days, accepted in upper or lower case.
     * The suffixes must occur in order.
     * The number part of each section must consist of ASCII digits.
     * The number may be prefixed by the ASCII negative or positive symbol.
     * The number must parse to an `int`.
     *
     *
     * The leading plus/minus sign, and negative values for other units are
     * not part of the ISO-8601 standard. In addition, ISO-8601 does not
     * permit mixing between the `PnYnMnD` and `PnW` formats.
     * Any week-based input is multiplied by 7 and treated as a number of days.
     *
     *
     * For example, the following are valid inputs:
     * <pre>
     * "P2Y"             -- Period.ofYears(2)
     * "P3M"             -- Period.ofMonths(3)
     * "P4W"             -- Period.ofWeeks(4)
     * "P5D"             -- Period.ofDays(5)
     * "P1Y2M3D"         -- Period.of(1, 2, 3)
     * "P1Y2M3W4D"       -- Period.of(1, 2, 25)
     * "P-1Y2M"          -- Period.of(-1, 2, 0)
     * "-P1Y2M"          -- Period.of(-1, -2, 0)
    </pre> *
     *
     * @param text  the text to parse, not null
     * @return the parsed period, not null
     * @throws DateTimeParseException if the text cannot be parsed to a period
     */
    fun parse(text: CharSequence): Period? {
      Objects.requireNonNull(text, "text")
      val matcher: Matcher = PATTERN.matcher(text)
      if (matcher.matches()) {
        val negate = if ("-" == matcher.group(1)) -1 else 1
        val yearMatch = matcher.group(2)
        val monthMatch = matcher.group(3)
        val weekMatch = matcher.group(4)
        val dayMatch = matcher.group(5)
        if (yearMatch != null || monthMatch != null || dayMatch != null || weekMatch != null) {
          return try {
            val years: Int = parseNumber(yearMatch, negate)
            val months: Int = parseNumber(monthMatch, negate)
            val weeks: Int = parseNumber(weekMatch, negate)
            val days: Int = parseNumber(dayMatch, negate)
            Period(years, months, weeks, days)
          } catch (ex: NumberFormatException) {
            null
          }
        }
      }
      return null
    }

    private fun multiplyExact(x: Int, y: Int): Int {
      val r = x.toLong() * y.toLong()
      return r.toInt()
    }

    private fun parseNumber(str: String?, negate: Int): Int {
      if (str == null) {
        return 0
      }
      val number = str.toInt()
      return try {
        multiplyExact(number, negate)
      } catch (ex: ArithmeticException) {
        throw Exception("Text cannot be parsed to a Period")
      }
    }
  }

  fun mapToSubsFrequency(context: Context, fiatText: String): String {
    return when {
      years == 1 -> context.getString(R.string.subscriptions_per_year, fiatText)
      years > 1 -> context.getString(R.string.subscriptions_per_several_year, fiatText)
      months == 1 -> context.getString(R.string.subscriptions_per_month, fiatText)
      months > 1 -> context.getString(R.string.subscriptions_per_several_month, fiatText)
      weeks == 1 -> context.getString(R.string.subscriptions_per_week, fiatText)
      weeks > 1 -> context.getString(R.string.subscriptions_per_several_week, fiatText)
      days == 1 -> context.getString(R.string.subscriptions_per_day, fiatText)
      days > 1 -> context.getString(R.string.subscriptions_per_several_day, fiatText)
      else -> fiatText
    }
  }
}