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

fun BigDecimal.formatWithSuffix(): String {
  val suffixformatter = NumberFormatterUtils.create()
  val scaledNumber = this.setScale(2, RoundingMode.FLOOR)
  return suffixformatter.formatNumberWithSuffix(scaledNumber.toFloat(), 2)
}