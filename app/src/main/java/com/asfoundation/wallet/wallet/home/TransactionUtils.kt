package com.asfoundation.wallet.wallet.home

import androidx.compose.ui.text.style.TextDecoration
import com.asf.wallet.R
import com.asfoundation.wallet.transactions.Transaction.TransactionCardInfo
import com.asfoundation.wallet.transactions.TransactionModel
import com.asfoundation.wallet.transactions.TransactionType.*

fun TransactionModel.cardInfoByType() = when (this.type) {
  PURCHASE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reward,
    title = R.string.transaction_type_purchase_bonus,
    description = description,
    amount = mainAmount,
    currency = convertedAmount
  )

  TOP_UP -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_topup,
    title = R.string.transaction_type_topup,
    amount = mainAmount,
    currency = convertedAmount
  )

  GIFT_CARD -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_gift,
    title = R.string.transaction_type_gift_card,
    amount = mainAmount,
    currency = convertedAmount
  )

  EXTRA_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_gift,
    title = R.string.transaction_type_extra_bonus,
    amount = mainAmount,
    description = description,
    currency = convertedAmount
  )

  PROMO_CODE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_gift,
    title = R.string.transaction_type_promo_code_bonus,
    amount = mainAmount,
    currency = convertedAmount
  )

  REVERTED_PURCHASE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_reward,
    title = R.string.transaction_type_reverted_purchase_bonus,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_refund_reverted_mini
  )

  REVERTED_EXTRA_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_gift,
    title = R.string.transaction_type_reverted_extra_bonus,
    amount = mainAmount,
    description = description,
    subIcon = R.drawable.ic_transaction_refund_reverted_mini,
    currency = convertedAmount
  )

  REVERTED_PROMO_CODE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_gift,
    title = R.string.transaction_type_reverted_promo_code_bonus,
    amount = mainAmount,
    description = description,
    subIcon = R.drawable.ic_transaction_refund_reverted_mini,
    currency = convertedAmount
  )

  E_SKILLS_REWARD -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reward,
    title = R.string.transaction_type_eskills_reward,
    description = description,
    amount = mainAmount,
    currency = convertedAmount
  )

  E_SKILLS_TICKET_REFUND -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_eskills_ticke_refund,
    description = description,
    amount = mainAmount,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_refund_reverted_mini
  )

  REJECTED_E_SKILLS_TICKET -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_rejected_eskills_ticket,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_rejected_mini,
    textDecoration = TextDecoration.LineThrough
  )

  FUNDS_SENT -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_transfer,
    title = R.string.transaction_type_funds_sent,
    amount = mainAmount,
    description = description,
    currency = convertedAmount
  )

  FUNDS_RECEIVED -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_transfer,
    title = R.string.transaction_type_funds_received,
    amount = mainAmount,
    description = description,
    currency = convertedAmount
  )

  IN_APP_PURCHASE -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_iab,
    amount = mainAmount,
    description = description,
    currency = convertedAmount
  )

  PURCHASE_REFUND -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_reward,
    title = R.string.transaction_type_reverted_purchase_title,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_refund_reverted_mini
  )

  REJECTED_PURCHASE -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_reward,
    title = R.string.transaction_type_rejected_purchase,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_rejected_mini,
    textDecoration = TextDecoration.LineThrough
  )

  REVERTED_TOP_UP -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted,
    title = R.string.transaction_type_reverted_topup,
    amount = mainAmount,
    currency = convertedAmount,
  )

  REJECTED_TOP_UP -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_rejected_topup,
    title = R.string.transaction_type_rejected_topup,
    amount = mainAmount,
    currency = convertedAmount,
    textDecoration = TextDecoration.LineThrough
  )

  SUBSCRIPTION_PAYMENT -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_subscription_payment,
    amount = mainAmount,
    description = description,
    currency = convertedAmount
  )

  SUBSCRIPTION_REFUND -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_refund_subscription,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_refund_reverted_mini
  )

  REJECTED_SUBSCRIPTION_PURCHASE -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_rejected_subscription_purchase,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_rejected_mini,
    textDecoration = TextDecoration.LineThrough
  )

  E_SKILLS_ENTRY_TICKET -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.transaction_type_eskills,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
  )

  OTHER -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_transfer,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
  )
}