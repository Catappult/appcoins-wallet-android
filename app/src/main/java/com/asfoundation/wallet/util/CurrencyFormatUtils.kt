package com.asfoundation.wallet.util

import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

class CurrencyFormatUtils {

  companion object {
    fun create(): CurrencyFormatUtils = CurrencyFormatUtils()
    const val FIAT_SCALE = 2
    const val APPC_SCALE = 2
    const val CREDITS_SCALE = 2
    const val ETH_SCALE = 4
  }

  fun formatCurrency(value: Double, currencyType: WalletCurrency): String {
    return when (currencyType) {
      WalletCurrency.FIAT -> formatCurrencyFiat(value)
      WalletCurrency.APPCOINS -> formatCurrencyAppcoins(value)
      WalletCurrency.CREDITS -> formatCurrencyCredits(value)
      WalletCurrency.ETHEREUM -> formatCurrencyEth(value)
    }
  }

  fun formatCurrency(value: String, currencyType: WalletCurrency): String {
    return when (currencyType) {
      WalletCurrency.FIAT -> formatCurrencyFiat(value.toDouble())
      WalletCurrency.APPCOINS -> formatCurrencyAppcoins(value.toDouble())
      WalletCurrency.CREDITS -> formatCurrencyCredits(value.toDouble())
      WalletCurrency.ETHEREUM -> formatCurrencyEth(value.toDouble())
    }
  }

  fun formatCurrency(fiatValue: FiatValue): String {
    val value = fiatValue.amount.toDouble()
    return formatCurrencyFiat(value)
  }

  private fun formatCurrencyFiat(value: Double): String {
    val fiatFormatter = NumberFormat.getNumberInstance()
        .apply {
          minimumFractionDigits = FIAT_SCALE
          maximumFractionDigits = FIAT_SCALE
          roundingMode = RoundingMode.FLOOR
        }
    return fiatFormatter.format(value)
  }

  private fun formatCurrencyAppcoins(value: Double): String {
    val appcFormatter = NumberFormat.getNumberInstance()
        .apply {
          minimumFractionDigits = APPC_SCALE
          maximumFractionDigits = APPC_SCALE
          roundingMode = RoundingMode.FLOOR
        }
    return appcFormatter.format(value)
  }

  private fun formatCurrencyCredits(value: Double): String {
    val creditsFormatter = NumberFormat.getNumberInstance()
        .apply {
          minimumFractionDigits = CREDITS_SCALE
          maximumFractionDigits = CREDITS_SCALE
          roundingMode = RoundingMode.FLOOR
        }
    return creditsFormatter.format(value)
  }

  private fun formatCurrencyEth(value: Double): String {
    val ethFormatter = NumberFormat.getNumberInstance()
        .apply {
          minimumFractionDigits = ETH_SCALE
          maximumFractionDigits = ETH_SCALE
          roundingMode = RoundingMode.FLOOR
        }
    return ethFormatter.format(value)
  }

  fun formatTransferConfirmation(value: Double, currencyType: WalletCurrency): String {
    val scale: Int = when (currencyType) {
      WalletCurrency.APPCOINS -> APPC_SCALE
      WalletCurrency.CREDITS -> CREDITS_SCALE
      else -> ETH_SCALE
    }
    val transferFormatter = NumberFormat.getNumberInstance()
        .apply {
          minimumFractionDigits = scale
          maximumFractionDigits = 18
          roundingMode = RoundingMode.FLOOR
        }
    return transferFormatter.format(value)
  }

  fun scaleFiat(value: BigDecimal): BigDecimal {
    return value.setScale(FIAT_SCALE, BigDecimal.ROUND_FLOOR)
  }
}

enum class WalletCurrency(val symbol: String) {
  FIAT(""),
  APPCOINS("APPC"),
  CREDITS("APPC-C"),
  ETHEREUM("ETH")
}