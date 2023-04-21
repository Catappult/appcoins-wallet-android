package com.appcoins.wallet.core.utils.android_common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import javax.inject.Inject
import kotlin.math.pow

class CurrencyFormatUtils @Inject constructor() {

  companion object {
    const val FIAT_SCALE = 2
    const val APPC_SCALE = 2
    const val CREDITS_SCALE = 2
    const val ETH_SCALE = 4
    val BILLION = BigDecimal(1_000_000_000)
    val MILLION = BigDecimal(1_000_000)
    val THOUSAND = BigDecimal(1_000)
  }

  fun formatCurrency(value: BigDecimal, currencyType: WalletCurrency): String {
    return when (currencyType) {
      WalletCurrency.FIAT -> formatCurrencyFiat(value.toDouble())
      WalletCurrency.APPCOINS -> formatCurrencyAppcoins(value.toDouble())
      WalletCurrency.CREDITS -> formatCurrencyCredits(value.toDouble())
      WalletCurrency.ETHEREUM -> formatCurrencyEth(value.toDouble())
    }
  }

  fun getScaledValue(
    valueStr: String, decimals: Long, currencySymbol: String,
    flipSign: Boolean
  ): String {
    val sign = if (flipSign) -1 else 1
    val walletCurrency = WalletCurrency.mapToWalletCurrency(currencySymbol);
    val value = BigDecimal(valueStr).divide(
      BigDecimal(
        10.toDouble()
          .pow(decimals.toDouble())
      )
    )
      .multiply(sign.toBigDecimal())
    // In case of positive value, we need to explicitly put the "+" sign
    val signedString = if (value > BigDecimal.ZERO) "+" else ""
    return signedString + formatCurrency(value, walletCurrency)
  }

  fun formatCurrency(value: String, currencyType: WalletCurrency): String {
    return when (currencyType) {
      WalletCurrency.FIAT -> formatCurrencyFiat(value.toDouble())
      WalletCurrency.APPCOINS -> formatCurrencyAppcoins(value.toDouble())
      WalletCurrency.CREDITS -> formatCurrencyCredits(value.toDouble())
      WalletCurrency.ETHEREUM -> formatCurrencyEth(value.toDouble())
    }
  }

  fun formatCurrency(amount: BigDecimal): String {
    val value = amount.toDouble()
    return formatCurrencyFiat(value)
  }

  private fun formatCurrencyFiat(
    value: Double,
    rounding: RoundingMode = RoundingMode.FLOOR
  ): String {
    val fiatFormatter = NumberFormat.getNumberInstance()
      .apply {
        minimumFractionDigits = FIAT_SCALE
        maximumFractionDigits = FIAT_SCALE
        roundingMode = rounding
      }
    return fiatFormatter.format(value)
  }

  private fun formatCurrencyAppcoins(
    value: Double,
    rounding: RoundingMode = RoundingMode.FLOOR
  ): String {
    val appcFormatter = NumberFormat.getNumberInstance()
      .apply {
        minimumFractionDigits = APPC_SCALE
        maximumFractionDigits = APPC_SCALE
        roundingMode = rounding
      }
    return appcFormatter.format(value)
  }

  private fun formatCurrencyCredits(
    value: Double,
    rounding: RoundingMode = RoundingMode.FLOOR
  ): String {
    val creditsFormatter = NumberFormat.getNumberInstance()
      .apply {
        minimumFractionDigits = CREDITS_SCALE
        maximumFractionDigits = CREDITS_SCALE
        roundingMode = rounding
      }
    return creditsFormatter.format(value)
  }

  private fun formatCurrencyEth(
    value: Double,
    rounding: RoundingMode = RoundingMode.FLOOR
  ): String {
    val ethFormatter = NumberFormat.getNumberInstance()
      .apply {
        minimumFractionDigits = ETH_SCALE
        maximumFractionDigits = ETH_SCALE
        roundingMode = rounding
      }
    return ethFormatter.format(value)
  }

  fun formatTransferCurrency(value: BigDecimal, currencyType: WalletCurrency): String {
    val scale: Int = when (currencyType) {
      WalletCurrency.APPCOINS -> APPC_SCALE
      WalletCurrency.CREDITS -> CREDITS_SCALE
      WalletCurrency.ETHEREUM -> ETH_SCALE
      else -> throw IllegalArgumentException()
    }
    val transferFormatter = DecimalFormat("#,##0.00")
      .apply {
        minimumFractionDigits = scale
        maximumFractionDigits = 15
        isParseBigDecimal = true
        roundingMode = RoundingMode.FLOOR
      }
    return transferFormatter.format(value)
  }

  fun formatPaymentCurrency(value: BigDecimal, currencyType: WalletCurrency): String {
    return when (currencyType) {
      WalletCurrency.FIAT -> formatCurrencyFiat(value.toDouble(), RoundingMode.CEILING)
      WalletCurrency.APPCOINS -> formatCurrencyAppcoins(value.toDouble(), RoundingMode.CEILING)
      WalletCurrency.CREDITS -> formatCurrencyCredits(value.toDouble(), RoundingMode.CEILING)
      WalletCurrency.ETHEREUM -> formatCurrencyEth(value.toDouble(), RoundingMode.CEILING)
    }
  }

  fun formatGamificationValues(value: BigDecimal): String {
    val formatter = DecimalFormat("#,###.##")
    return formatter.format(value)
  }

  val formatShortGamificationValues: (value: BigDecimal) -> String = { value ->
    when {
      value >= BILLION -> "${formatGamificationValues(value.divide(BILLION))}b"
      value >= MILLION -> "${formatGamificationValues(value.divide(MILLION))}m"
      value >= THOUSAND -> "${formatGamificationValues(value.divide(THOUSAND))}k"
      else -> formatGamificationValues(value)
    }
  }

  fun scaleFiat(value: BigDecimal): BigDecimal = value.setScale(FIAT_SCALE, BigDecimal.ROUND_FLOOR)
}


enum class WalletCurrency(val symbol: String) {
  FIAT(""),
  APPCOINS("APPC"),
  CREDITS("APPC-C"),
  ETHEREUM("ETH");

  companion object {
    @JvmStatic
    fun mapToWalletCurrency(currencySymbol: String): WalletCurrency {
      return when (currencySymbol) {
        "APPC" -> APPCOINS
        "ETH" -> ETHEREUM
        "APPC-C", "AppCoins Credits" -> CREDITS
        "" -> FIAT
        else -> throw IllegalArgumentException()
      }
    }
  }
}