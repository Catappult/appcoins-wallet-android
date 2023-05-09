package com.asfoundation.wallet.wallet.home

import androidx.compose.ui.text.style.TextDecoration
import com.asf.wallet.R
import com.asfoundation.wallet.transactions.Transaction.TransactionCardInfo
import com.asfoundation.wallet.transactions.TransactionModel
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

fun TransactionModel.cardInfoByType() = when (this.type) {
  PURCHASE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reward,
    title = R.string.transaction_type_purchase_bonus,
    description = description,
    amount = mainAmount,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  TOP_UP -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_topup,
    title = R.string.transaction_type_topup,
    amount = mainAmount,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  GIFT_CARD -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_gift,
    title = R.string.transaction_type_gift_card,
    amount = mainAmount,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  EXTRA_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_gift,
    title = R.string.transaction_type_extra_bonus,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  PROMO_CODE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_gift,
    title = R.string.transaction_type_promo_code_bonus,
    amount = mainAmount,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  REVERTED_PURCHASE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_reward,
    title = R.string.transaction_type_reverted_purchase_bonus,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,
    subIcon = R.drawable.ic_transaction_refund_reverted_mini
  )

  REVERTED_EXTRA_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_gift,
    title = R.string.transaction_type_reverted_extra_bonus,
    amount = mainAmount,
    description = description,
    subIcon = R.drawable.ic_transaction_refund_reverted_mini,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  REVERTED_PROMO_CODE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_gift,
    title = R.string.transaction_type_reverted_promo_code_bonus,
    amount = mainAmount,
    description = description,
    subIcon = R.drawable.ic_transaction_refund_reverted_mini,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  E_SKILLS_REWARD -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reward,
    title = R.string.transaction_type_eskills_reward,
    description = description,
    amount = mainAmount,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  E_SKILLS_TICKET_REFUND -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_eskills_ticke_refund,
    description = description,
    amount = mainAmount,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    subIcon = R.drawable.ic_transaction_refund_reverted_mini
  )

  REJECTED_E_SKILLS_TICKET -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_rejected_eskills_ticket,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    subIcon = R.drawable.ic_transaction_rejected_mini,
    textDecoration = TextDecoration.LineThrough
  )

  FUNDS_SENT -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_transfer,
    title = R.string.transaction_type_funds_sent,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  FUNDS_RECEIVED -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_transfer,
    title = R.string.transaction_type_funds_received,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  IN_APP_PURCHASE -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_iab,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  PURCHASE_REFUND -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_reward,
    title = R.string.transaction_type_reverted_purchase_title,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    subIcon = R.drawable.ic_transaction_refund_reverted_mini
  )

  REJECTED_PURCHASE -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_reward,
    title = R.string.transaction_type_rejected_purchase,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    subIcon = R.drawable.ic_transaction_rejected_mini,
    textDecoration = TextDecoration.LineThrough
  )

  REVERTED_TOP_UP -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted,
    title = R.string.transaction_type_reverted_topup,
    amount = mainAmount,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    )

  REJECTED_TOP_UP -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_rejected_topup,
    title = R.string.transaction_type_rejected_topup,
    amount = mainAmount,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    textDecoration = TextDecoration.LineThrough
  )

  SUBSCRIPTION_PAYMENT -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_subscription_payment,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl
  )

  SUBSCRIPTION_REFUND -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_refund_subscription,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    subIcon = R.drawable.ic_transaction_refund_reverted_mini
  )

  REJECTED_SUBSCRIPTION_PURCHASE -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_rejected_subscription_purchase,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    subIcon = R.drawable.ic_transaction_rejected_mini,
    textDecoration = TextDecoration.LineThrough
  )

  E_SKILLS_ENTRY_TICKET -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_eskills,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    )

  OTHER -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_transfer,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    convertedAmount = convertedAmount,
    date = date,
    status = status,
    transactionUrl = transactionUrl,

    )
}