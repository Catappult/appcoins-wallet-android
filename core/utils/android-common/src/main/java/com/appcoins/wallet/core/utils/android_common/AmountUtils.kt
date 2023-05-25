package com.appcoins.wallet.core.utils.android_common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

object AmountUtils {
  private const val TOKEN_DECIMALS = 18

  fun String?.formatMoney(currencySymbol: String = "", sign: String = ""): String? =
    if (this == null) this else sign + currencySymbol + numberFormatter().format(BigDecimal(this))

  fun String.format18decimals(sign: String = ""): String {
    val value = BigDecimal(this).divide(BigDecimal.TEN.pow(TOKEN_DECIMALS))
    return sign + numberFormatter().format(value)
  }

  private fun numberFormatter(): NumberFormat =
    NumberFormat.getNumberInstance().apply {
      minimumFractionDigits = CurrencyFormatUtils.DEFAULT_SCALE
      maximumFractionDigits = CurrencyFormatUtils.DEFAULT_SCALE
      roundingMode = RoundingMode.FLOOR
    }
}