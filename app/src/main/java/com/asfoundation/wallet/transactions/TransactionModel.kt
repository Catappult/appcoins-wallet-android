package com.asfoundation.wallet.transactions

import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils.Companion.DEFAULT_SCALE
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils
import com.asfoundation.wallet.C.ETHER_DECIMALS
import com.asfoundation.wallet.transactions.TransactionType.BURN
import com.asfoundation.wallet.transactions.TransactionType.EXTRA_BONUS
import com.asfoundation.wallet.transactions.TransactionType.E_SKILLS_ENTRY_TICKET
import com.asfoundation.wallet.transactions.TransactionType.E_SKILLS_REWARD
import com.asfoundation.wallet.transactions.TransactionType.E_SKILLS_TICKET_REFUND
import com.asfoundation.wallet.transactions.TransactionType.E_SKILLS_WITHDRAW
import com.asfoundation.wallet.transactions.TransactionType.FEE
import com.asfoundation.wallet.transactions.TransactionType.FUNDS_RECEIVED
import com.asfoundation.wallet.transactions.TransactionType.FUNDS_SENT
import com.asfoundation.wallet.transactions.TransactionType.GIFT_CARD
import com.asfoundation.wallet.transactions.TransactionType.IN_APP_PURCHASE
import com.asfoundation.wallet.transactions.TransactionType.OTHER
import com.asfoundation.wallet.transactions.TransactionType.PROMO_CODE_BONUS
import com.asfoundation.wallet.transactions.TransactionType.PURCHASE_BONUS
import com.asfoundation.wallet.transactions.TransactionType.PURCHASE_REFUND
import com.asfoundation.wallet.transactions.TransactionType.REJECTED_E_SKILLS_TICKET
import com.asfoundation.wallet.transactions.TransactionType.REJECTED_PURCHASE
import com.asfoundation.wallet.transactions.TransactionType.REJECTED_SUBSCRIPTION_PURCHASE
import com.asfoundation.wallet.transactions.TransactionType.REJECTED_TOP_UP
import com.asfoundation.wallet.transactions.TransactionType.REVERTED_EXTRA_BONUS
import com.asfoundation.wallet.transactions.TransactionType.REVERTED_PROMO_CODE_BONUS
import com.asfoundation.wallet.transactions.TransactionType.REVERTED_PURCHASE_BONUS
import com.asfoundation.wallet.transactions.TransactionType.REVERTED_TOP_UP
import com.asfoundation.wallet.transactions.TransactionType.SUBSCRIPTION_PAYMENT
import com.asfoundation.wallet.transactions.TransactionType.SUBSCRIPTION_REFUND
import com.asfoundation.wallet.transactions.TransactionType.TOP_UP
import com.asfoundation.wallet.transactions.TransactionType.VOUCHER_PURCHASE
import com.asfoundation.wallet.transactions.TransactionType.WITHDRAW
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency

data class TransactionModel(
  val id: String,
  val type: TransactionType,
  val date: String,
  val mainAmount: String,
  val description: String?,
  val appIcon: String?,
  val convertedAmount: String
)

fun TransactionResponse.toModel(): TransactionModel {
  return TransactionModel(
    id = txId,
    type = type.toTransactionType(),
    date = DateFormatterUtils.getDate(processedTime),
    appIcon = appIcon,
    description = bonusDescription ?: app,
    mainAmount = paidAmount() ?: amount(),
    convertedAmount = if (paidAmount() == null) amountCurrency else "${amount()} $amountCurrency",
  )
}

enum class TransactionType {
  GIFT_CARD,
  PURCHASE_BONUS,
  EXTRA_BONUS,
  PROMO_CODE_BONUS,
  REVERTED_PURCHASE_BONUS,
  REVERTED_EXTRA_BONUS,
  REVERTED_PROMO_CODE_BONUS,
  BURN,
  E_SKILLS_ENTRY_TICKET,
  E_SKILLS_WITHDRAW,
  E_SKILLS_REWARD,
  E_SKILLS_TICKET_REFUND,
  REJECTED_E_SKILLS_TICKET,
  FEE,
  FUNDS_SENT,
  FUNDS_RECEIVED,
  IN_APP_PURCHASE,
  PURCHASE_REFUND,
  REJECTED_PURCHASE,
  WITHDRAW,
  TOP_UP,
  REVERTED_TOP_UP,
  REJECTED_TOP_UP,
  SUBSCRIPTION_PAYMENT,
  SUBSCRIPTION_REFUND,
  REJECTED_SUBSCRIPTION_PURCHASE,
  VOUCHER_PURCHASE,
  OTHER
}

fun String.toTransactionType(): TransactionType {
  return when (this) {
    "Gift Card" -> GIFT_CARD
    "Purchase Bonus" -> PURCHASE_BONUS
    "Extra Bonus" -> EXTRA_BONUS
    "Promo Code Bonus" -> PROMO_CODE_BONUS
    "Reverted Purchase Bonus" -> REVERTED_PURCHASE_BONUS
    "Reverted Extra Bonus" -> REVERTED_EXTRA_BONUS
    "Reverted Promo Code Bonus" -> REVERTED_PROMO_CODE_BONUS
    "Burn" -> BURN
    "e-Skills Entry Ticket" -> E_SKILLS_ENTRY_TICKET
    "e-Skills Withdraw" -> E_SKILLS_WITHDRAW
    "e-Skills Reward" -> E_SKILLS_REWARD
    "e-Skills Ticket Refund" -> E_SKILLS_TICKET_REFUND
    "Rejected e-Skills Ticket" -> REJECTED_E_SKILLS_TICKET
    "Fee" -> FEE
    "Funds Sent" -> FUNDS_SENT
    "Funds Received" -> FUNDS_RECEIVED
    "In-App Purchase" -> IN_APP_PURCHASE
    "Purchase Refund" -> PURCHASE_REFUND
    "Rejected Purchase" -> REJECTED_PURCHASE
    "Withdraw" -> WITHDRAW
    "Top-Up" -> TOP_UP
    "Reverted Top-Up" -> REVERTED_TOP_UP
    "Rejected Top-Up" -> REJECTED_TOP_UP
    "Subscription Payment" -> SUBSCRIPTION_PAYMENT
    "Subscription Refund" -> SUBSCRIPTION_REFUND
    "Rejected Subscription Purchase" -> REJECTED_SUBSCRIPTION_PURCHASE
    "Voucher Purchase" -> VOUCHER_PURCHASE
    else -> OTHER
  }
}


private fun TransactionResponse.paidAmount(): String? =
  (defaultCurrencyAmount ?: paidCurrencyAmount).formatMoney()
    .addCurrencyAndSign(paidCurrency.currencySymbol())

private fun TransactionResponse.amount() = amount.format18decimals()

fun String?.currencySymbol(): String =
  try {
    Currency.getInstance(this).symbol
  } catch (e: Exception) {
    ""
  }

fun String?.formatMoney(): String? = if (this == null) this else
  numberFormatter().format(BigDecimal(this))

fun String.format18decimals(): String {
  val value = BigDecimal(this).divide(BigDecimal.TEN.pow(ETHER_DECIMALS))
  return numberFormatter().format(value).addSign(value > BigDecimal.ZERO)
}

fun String?.addCurrencyAndSign(currencySymbol: String): String? {
  return if (this != null) {
    val prefix = if (BigDecimal(this) > BigDecimal.ZERO) "+$currencySymbol" else currencySymbol
    prefix + this
  } else this
}

fun String.addSign(isPositive: Boolean) = if (isPositive) "+$this" else this

fun numberFormatter(): NumberFormat = NumberFormat.getNumberInstance()
  .apply {
    minimumFractionDigits = DEFAULT_SCALE
    maximumFractionDigits = DEFAULT_SCALE
    roundingMode = RoundingMode.FLOOR
  }