package com.asfoundation.wallet.util

import com.asfoundation.wallet.ui.transact.TransferFragmentView
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

class CurrencyFormatUtils {

  companion object {
    fun create(): CurrencyFormatUtils = CurrencyFormatUtils()
    const val FIAT_SCALE = 2
    const val APPC_SCALE = 2
    const val CREDITS_SCALE = 2
    const val ETH_SCALE = 4
  }

  fun formatCurrency(value: BigDecimal, currencyType: WalletCurrency): String {
    return when (currencyType) {
      WalletCurrency.FIAT -> formatCurrencyFiat(value.toDouble())
      WalletCurrency.APPCOINS -> formatCurrencyAppcoins(value.toDouble())
      WalletCurrency.CREDITS -> formatCurrencyCredits(value.toDouble())
      WalletCurrency.ETHEREUM -> formatCurrencyEth(value.toDouble())
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

  fun formatCurrency(amount: BigDecimal): String {
    val value = amount.toDouble()
    return formatCurrencyFiat(value)
  }

  private fun formatCurrencyFiat(value: Double,
                                 rounding: RoundingMode? = RoundingMode.HALF_DOWN): String {
    val fiatFormatter = NumberFormat.getNumberInstance()
        .apply {
          minimumFractionDigits = FIAT_SCALE
          maximumFractionDigits = FIAT_SCALE
          roundingMode = rounding
        }
    return fiatFormatter.format(value)
  }

  private fun formatCurrencyAppcoins(value: Double,
                                     rounding: RoundingMode? = RoundingMode.HALF_DOWN): String {
    val appcFormatter = NumberFormat.getNumberInstance()
        .apply {
          minimumFractionDigits = APPC_SCALE
          maximumFractionDigits = APPC_SCALE
          roundingMode = rounding
        }
    return appcFormatter.format(value)
  }

  private fun formatCurrencyCredits(value: Double,
                                    rounding: RoundingMode? = RoundingMode.HALF_DOWN): String {
    val creditsFormatter = NumberFormat.getNumberInstance()
        .apply {
          minimumFractionDigits = CREDITS_SCALE
          maximumFractionDigits = CREDITS_SCALE
          roundingMode = rounding
        }
    return creditsFormatter.format(value)
  }

  private fun formatCurrencyEth(value: Double,
                                rounding: RoundingMode? = RoundingMode.FLOOR): String {
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
        "APPC-C" -> CREDITS
        "" -> FIAT
        else -> throw IllegalArgumentException()
      }
    }

    @JvmStatic
    fun mapToWalletCurrency(currency: TransferFragmentView.Currency): WalletCurrency {
      return when (currency) {
        TransferFragmentView.Currency.APPC -> APPCOINS
        TransferFragmentView.Currency.APPC_C -> CREDITS
        TransferFragmentView.Currency.ETH -> ETHEREUM
      }
    }
  }
}