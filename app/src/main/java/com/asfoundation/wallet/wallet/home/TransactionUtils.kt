package com.asfoundation.wallet.wallet.home

import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.Transaction.TransactionType.ADS
import com.asfoundation.wallet.transactions.Transaction.TransactionType.ADS_OFFCHAIN
import com.asfoundation.wallet.transactions.Transaction.TransactionType.BONUS
import com.asfoundation.wallet.transactions.Transaction.TransactionType.BONUS_REVERT
import com.asfoundation.wallet.transactions.Transaction.TransactionType.ESKILLS
import com.asfoundation.wallet.transactions.Transaction.TransactionType.ESKILLS_REWARD
import com.asfoundation.wallet.transactions.Transaction.TransactionType.ETHER_TRANSFER
import com.asfoundation.wallet.transactions.Transaction.TransactionType.IAP
import com.asfoundation.wallet.transactions.Transaction.TransactionType.IAP_OFFCHAIN
import com.asfoundation.wallet.transactions.Transaction.TransactionType.IAP_REVERT
import com.asfoundation.wallet.transactions.Transaction.TransactionType.STANDARD
import com.asfoundation.wallet.transactions.Transaction.TransactionType.SUBS_OFFCHAIN
import com.asfoundation.wallet.transactions.Transaction.TransactionType.TOP_UP
import com.asfoundation.wallet.transactions.Transaction.TransactionType.TOP_UP_REVERT
import com.asfoundation.wallet.transactions.Transaction.TransactionType.TRANSFER
import com.asfoundation.wallet.transactions.Transaction.TransactionType.TRANSFER_OFF_CHAIN
import com.asfoundation.wallet.transactions.TransactionModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import kotlin.math.pow

fun Transaction.cardInfoByType() = when (this.type) {
  BONUS -> Transaction.TransactionCardInfo(
    icon = R.drawable.ic_transaction_reward,
    title = R.string.transaction_type_bonus,
    amount = getScaledValue(value, C.ETHER_DECIMALS.toLong(), "", false),
    currency = currency
  )

  TOP_UP -> Transaction.TransactionCardInfo(
    icon = R.drawable.ic_transaction_topup,
    title = R.string.topup_title,
    amount = "${
      getScaledValue(
        paidAmount!!,
        0,
        currency ?: "",
        false
      )
    } $paidCurrency",
    currency = "${
      getScaledValue(
        value,
        C.ETHER_DECIMALS.toLong(),
        currency ?: "",
        false
      )
    } $currency"
  )

  STANDARD -> TODO()
  IAP -> TODO()
  ADS -> TODO()
  IAP_OFFCHAIN -> TODO()
  ADS_OFFCHAIN -> TODO()
  TRANSFER_OFF_CHAIN -> TODO()
  BONUS_REVERT -> TODO()
  TOP_UP_REVERT -> TODO()
  IAP_REVERT -> TODO()
  SUBS_OFFCHAIN -> TODO()
  ESKILLS_REWARD -> TODO()
  ESKILLS -> TODO()
  TRANSFER -> TODO()
  ETHER_TRANSFER -> TODO()
}

fun TransactionModel.cardInfoByType() = when (this.type) {
  "Purchase Bonus" -> Transaction.TransactionCardInfo(
    icon = R.drawable.ic_transaction_reward,
    title = R.string.transaction_type_bonus,
    amount = mainAmount + mainCurrency,
    currency = convertedAmount + convertedCurrency
  )

  "Top-Up" -> Transaction.TransactionCardInfo(
    icon = R.drawable.ic_transaction_topup,
    title = R.string.topup_title,
    amount = mainAmount + mainCurrency,
    currency = convertedAmount + convertedCurrency
  )

  else -> TODO()
}

fun getScaledValue(
  valueStr: String, decimals: Long, currencySymbol: String,
  flipSign: Boolean
): String {
  val sign = if (flipSign) -1 else 1
  val walletCurrency = WalletCurrency.mapToWalletCurrency(currencySymbol)
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

fun formatCurrency(value: BigDecimal, currencyType: WalletCurrency): String {
  return when (currencyType) {
    WalletCurrency.FIAT -> formatCurrencyFiat(value.toDouble())
    WalletCurrency.APPCOINS -> formatCurrencyAppcoins(value.toDouble())
    WalletCurrency.CREDITS -> formatCurrencyCredits(value.toDouble())
    WalletCurrency.ETHEREUM -> formatCurrencyEth(value.toDouble())
  }
}

private fun formatCurrencyFiat(
  value: Double,
  rounding: RoundingMode = RoundingMode.FLOOR
): String {
  val fiatFormatter = NumberFormat.getNumberInstance()
    .apply {
      minimumFractionDigits = CurrencyFormatUtils.FIAT_SCALE
      maximumFractionDigits = CurrencyFormatUtils.FIAT_SCALE
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
      minimumFractionDigits = CurrencyFormatUtils.APPC_SCALE
      maximumFractionDigits = CurrencyFormatUtils.APPC_SCALE
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
      minimumFractionDigits = CurrencyFormatUtils.CREDITS_SCALE
      maximumFractionDigits = CurrencyFormatUtils.CREDITS_SCALE
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
      minimumFractionDigits = CurrencyFormatUtils.ETH_SCALE
      maximumFractionDigits = CurrencyFormatUtils.ETH_SCALE
      roundingMode = rounding
    }
  return ethFormatter.format(value)
}