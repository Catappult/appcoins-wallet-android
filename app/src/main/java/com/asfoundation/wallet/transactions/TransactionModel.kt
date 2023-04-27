package com.asfoundation.wallet.transactions

import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils.Companion.DEFAULT_SCALE
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils
import com.asfoundation.wallet.C.ETHER_DECIMALS
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency

data class TransactionModel(
  val id: String,
  val type: String,
  val date: String,
  val mainAmount: String,
  val appIcon: String?,
  val convertedAmount: String,
  val convertedCurrency: String
)

fun TransactionResponse.toModel(): TransactionModel {
  val formattedAmount = amount.format18decimals()
  val formattedDefaultCurrencyAmount = defaultCurrencyAmount.formatMoney()
  val formattedPaidCurrencyAmount = paidCurrencyAmount.formatMoney()

  return TransactionModel(
    id = txId,
    type = type,
    date = DateFormatterUtils.getDate(processedTime),
    appIcon = appIcon,
    mainAmount = (formattedDefaultCurrencyAmount ?: formattedPaidCurrencyAmount ?: formattedAmount)
      .addCurrencyAndSign(paidCurrency.currencySymbol() ?: ""),
    convertedAmount = if (formattedDefaultCurrencyAmount == null && formattedPaidCurrencyAmount == null) "" else formattedAmount,
    convertedCurrency = amountCurrency
  )
}

fun String?.currencySymbol(): String? =
  if (this == null) this else Currency.getInstance(this).symbol

fun String?.formatMoney(): String? = if (this == null) this else
  numberFormatter().format(BigDecimal(this))

fun String.format18decimals(): String {
  val value = BigDecimal(this).divide(BigDecimal.TEN.pow(ETHER_DECIMALS))
  return numberFormatter().format(value)
}

fun String.addCurrencyAndSign(currencySymbol: String) =
  if (BigDecimal(this) > BigDecimal.ZERO) "+$currencySymbol$this" else currencySymbol + this

fun numberFormatter(): NumberFormat = NumberFormat.getNumberInstance()
  .apply {
    minimumFractionDigits = DEFAULT_SCALE
    maximumFractionDigits = DEFAULT_SCALE
    roundingMode = RoundingMode.FLOOR
  }