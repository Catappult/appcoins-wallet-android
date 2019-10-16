package com.asfoundation.wallet.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 *
 * Class file to create kotlin extension functions
 *
 */

fun BigDecimal.scaleToString(scale: Int): String {
  val format = DecimalFormat("#.##")
  return format.format(this.setScale(scale, RoundingMode.FLOOR))
}

fun BigDecimal.formatWithSuffix(scale: Int): String {
  val suffixFormatter = NumberFormatterUtils.create()
  val scaledNumber = this.setScale(scale, RoundingMode.FLOOR)
  return suffixFormatter.formatNumberWithSuffix(scaledNumber.toFloat(), scale)
}