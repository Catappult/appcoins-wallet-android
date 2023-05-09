package com.asfoundation.wallet.transactions

import androidx.compose.ui.graphics.Color
import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils.Companion.DEFAULT_SCALE
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_green
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_red
import com.asf.wallet.R
import com.asfoundation.wallet.C.ETHER_DECIMALS
import com.asfoundation.wallet.transactions.StatusType.PENDING
import com.asfoundation.wallet.transactions.StatusType.REJECTED
import com.asfoundation.wallet.transactions.StatusType.REVERTED
import com.asfoundation.wallet.transactions.StatusType.SUCCESS
import com.asfoundation.wallet.transactions.TransactionType.EXTRA_BONUS
import com.asfoundation.wallet.transactions.TransactionType.E_SKILLS_ENTRY_TICKET
import com.asfoundation.wallet.transactions.TransactionType.E_SKILLS_REWARD
import com.asfoundation.wallet.transactions.TransactionType.E_SKILLS_TICKET_REFUND
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
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency

const val POSITIVE_SIGN = "+"
const val NEGATIVE_SIGN = "-"
const val CURRENCY_CODE_LENGHT = 3
const val TRANSACTION_BASE_URL = "https://appcexplorer.io/transaction/"

data class TransactionModel(
  val id: String,
  val status: StatusType,
  val type: TransactionType,
  val date: String,
  val mainAmount: String,
  val description: String?,
  val appIcon: String?,
  val convertedAmount: String,
  val from: String,
  val to: String,
  val transactionUrl: String
)

fun TransactionResponse.toModel(selectedCurrency: String): TransactionModel {
  return TransactionModel(
    id = txId,
    status = status.toStatusType(),
    type = type.toTransactionType(),
    date = DateFormatterUtils.getDate(processedTime),
    appIcon = appIcon,
    description = bonusDescription ?: app,
    mainAmount = paidAmount(selectedCurrency) ?: amount(),
    convertedAmount = if (paidAmount(selectedCurrency) == null) amountCurrency else "${amount()} $amountCurrency",
    from = sender,
    to = receiver,
    transactionUrl = "$TRANSACTION_BASE_URL$txId"
  )
}


enum class TransactionType(val sign: String = "") {
  TOP_UP(sign = POSITIVE_SIGN),
  GIFT_CARD(sign = POSITIVE_SIGN),
  PURCHASE_BONUS(sign = POSITIVE_SIGN),
  EXTRA_BONUS(sign = POSITIVE_SIGN),
  PROMO_CODE_BONUS(sign = POSITIVE_SIGN),
  E_SKILLS_REWARD(sign = POSITIVE_SIGN),
  E_SKILLS_TICKET_REFUND(sign = POSITIVE_SIGN),
  FUNDS_RECEIVED(sign = POSITIVE_SIGN),
  PURCHASE_REFUND(sign = POSITIVE_SIGN),
  SUBSCRIPTION_REFUND(sign = POSITIVE_SIGN),
  REJECTED_TOP_UP(sign = POSITIVE_SIGN),
  REJECTED_PURCHASE(sign = POSITIVE_SIGN),
  REJECTED_E_SKILLS_TICKET(sign = POSITIVE_SIGN),
  REJECTED_SUBSCRIPTION_PURCHASE(sign = POSITIVE_SIGN),
  SUBSCRIPTION_PAYMENT(sign = NEGATIVE_SIGN),
  REVERTED_PURCHASE_BONUS(sign = NEGATIVE_SIGN),
  REVERTED_EXTRA_BONUS(sign = NEGATIVE_SIGN),
  REVERTED_PROMO_CODE_BONUS(sign = NEGATIVE_SIGN),
  E_SKILLS_ENTRY_TICKET(sign = NEGATIVE_SIGN),
  FUNDS_SENT(sign = NEGATIVE_SIGN),
  IN_APP_PURCHASE(sign = NEGATIVE_SIGN),
  REVERTED_TOP_UP(sign = NEGATIVE_SIGN),
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
    "e-Skills Entry Ticket" -> E_SKILLS_ENTRY_TICKET
    "e-Skills Reward" -> E_SKILLS_REWARD
    "e-Skills Ticket Refund" -> E_SKILLS_TICKET_REFUND
    "Rejected e-Skills Ticket" -> REJECTED_E_SKILLS_TICKET
    "Funds Sent" -> FUNDS_SENT
    "Funds Received" -> FUNDS_RECEIVED
    "In-App Purchase" -> IN_APP_PURCHASE
    "Purchase Refund" -> PURCHASE_REFUND
    "Rejected Purchase" -> REJECTED_PURCHASE
    "Top-Up" -> TOP_UP
    "Reverted Top-Up" -> REVERTED_TOP_UP
    "Rejected Top-Up" -> REJECTED_TOP_UP
    "Subscription Payment" -> SUBSCRIPTION_PAYMENT
    "Subscription Refund" -> SUBSCRIPTION_REFUND
    "Rejected Subscription Purchase" -> REJECTED_SUBSCRIPTION_PURCHASE
    else -> OTHER
  }
}

enum class StatusType(val description: Int, val color: Color) {
  SUCCESS(description = R.string.transaction_status_success, color = styleguide_green),
  REJECTED(description = R.string.transaction_status_failed, color = styleguide_red),
  REVERTED(description = R.string.transaction_status_reverted, color = styleguide_red),
  PENDING(description = R.string.transaction_status_pending, color = styleguide_light_grey)
}

fun String.toStatusType() = when (this) {
  "Success" -> SUCCESS
  "Rejected" -> REJECTED
  "Reverted" -> REVERTED
  else -> PENDING
}


private fun TransactionResponse.paidAmount(selectedCurrency: String): String? {
  val sign = type.toTransactionType().sign
  return defaultCurrencyAmount.formatMoney(selectedCurrency.currencySymbol(), sign)
    ?: paidCurrencyAmount.formatMoney(paidCurrency.currencySymbol(), sign)
}

private fun TransactionResponse.amount() =
  amount.format18decimals(sign = type.toTransactionType().sign)

fun String?.currencySymbol(): String =
  if (this != null && this.length == CURRENCY_CODE_LENGHT) Currency.getInstance(this).symbol
  else ""

fun String?.formatMoney(currencySymbol: String, sign: String): String? =
  if (this == null) this else sign + currencySymbol + numberFormatter().format(BigDecimal(this))

fun String.format18decimals(sign: String): String {
  val value = BigDecimal(this).divide(BigDecimal.TEN.pow(ETHER_DECIMALS))
  return sign + numberFormatter().format(value)
}

fun numberFormatter(): NumberFormat =
  NumberFormat.getNumberInstance().apply {
    minimumFractionDigits = DEFAULT_SCALE
    maximumFractionDigits = DEFAULT_SCALE
    roundingMode = RoundingMode.FLOOR
  }
