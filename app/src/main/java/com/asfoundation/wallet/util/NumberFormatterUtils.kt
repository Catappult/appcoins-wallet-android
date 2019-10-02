package com.asfoundation.wallet.util

import java.util.*

class NumberFormatterUtils {

  companion object {
    fun create(): NumberFormatterUtils = NumberFormatterUtils()
    val suffixes = TreeMap<Long, String>()
  }

  init {
    suffixes[1_000L] = "k"
    suffixes[1_000_000L] = "M"
  }

  fun formatNumberWithSuffix(value: Long): String {
    if (value < 10000) return value.toString()

    val fetchLowestValueSuffix = suffixes.floorEntry(value)
    val divideBy = fetchLowestValueSuffix.key
    val suffix = fetchLowestValueSuffix.value

    val truncatedValue = value / (divideBy!! / 10)
    val hasDecimal =
        truncatedValue < 100 && truncatedValue / 10.0 != (truncatedValue / 10).toDouble()
    return if (hasDecimal) (truncatedValue / 10.0).toString() + suffix else (truncatedValue / 10).toString() + suffix
  }
}