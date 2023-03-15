package com.appcoins.wallet.core.utils.common

import java.text.DecimalFormat
import java.util.*

class NumberFormatterUtils {

  companion object {
    fun create(): NumberFormatterUtils = NumberFormatterUtils()
    val suffixes = TreeMap<Float, String>()
  }

  init {
    suffixes[1000f] = "k"
    suffixes[1000000f] = "M"
  }

  fun formatNumberWithSuffix(value: Float): String {
    if (value < 10000) return formatDecimalPlaces(value)

    val fetchLowestValueSuffix = suffixes.floorEntry(value)
    val divideBy = fetchLowestValueSuffix.key
    val suffix = fetchLowestValueSuffix.value

    val truncatedValue = value / (divideBy / 10)
    val hasDecimal =
      truncatedValue < 100 && truncatedValue / 10.0f != (truncatedValue / 10)
    return if (hasDecimal) {
      formatDecimalPlaces(truncatedValue / 10.0f) + suffix
    } else {
      formatDecimalPlaces((truncatedValue / 10)) + suffix
    }
  }

  fun formatNumberWithSuffix(value: Float, decimalPlaces: Int): String {
    if (decimalPlaces < 0) return value.toString()
    if (value < 10000) return formatDecimalPlaces(value)

    val fetchLowestValueSuffix = suffixes.floorEntry(value)
    val divideBy = fetchLowestValueSuffix.key
    val suffix = fetchLowestValueSuffix.value

    val truncatedValue = value / (divideBy / 10)
    var formatString = "#"
    if (decimalPlaces > 0) {
      formatString += "."
      for (i in 0 until decimalPlaces) {
        formatString += "#"
      }
    }
    val decimalFormatter = DecimalFormat(formatString)
    return decimalFormatter.format(truncatedValue) + suffix
  }

  private fun formatDecimalPlaces(value: Float): String {
    val splitValue = value.toString()
      .split(".")
    return if (splitValue[1] != "0") {
      value.toString()
    } else {
      removeDecimalPlaces(value)
    }
  }

  private fun removeDecimalPlaces(value: Float): String {
    val splitValue = value.toString()
      .split(".")
    return splitValue[0]
  }
}