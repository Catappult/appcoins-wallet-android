package com.asfoundation.wallet.wallet.home

import com.asf.wallet.R
import com.asfoundation.wallet.transactions.Transaction.TransactionCardInfo
import com.asfoundation.wallet.transactions.TransactionModel
import com.asfoundation.wallet.transactions.TransactionType.EXTRA_BONUS
import com.asfoundation.wallet.transactions.TransactionType.E_SKILLS_REWARD
import com.asfoundation.wallet.transactions.TransactionType.E_SKILLS_TICKET_REFUND
import com.asfoundation.wallet.transactions.TransactionType.FUNDS_RECEIVED
import com.asfoundation.wallet.transactions.TransactionType.FUNDS_SENT
import com.asfoundation.wallet.transactions.TransactionType.GIFT_CARD
import com.asfoundation.wallet.transactions.TransactionType.IN_APP_PURCHASE
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

fun TransactionModel.cardInfoByType() = when (this.type) {
  PURCHASE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reward,
    title = R.string.error_general,
    description = description,
    amount = mainAmount,
    currency = convertedAmount
  )

  TOP_UP -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_topup,
    title = R.string.topup_title,
    amount = mainAmount,
    currency = convertedAmount
  )

  GIFT_CARD -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_gift,
    title = R.string.error_general,
    amount = mainAmount,
    currency = convertedAmount
  )

  EXTRA_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_gift,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount
  )

  PROMO_CODE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_gift,
    title = R.string.error_general,
    amount = mainAmount,
    currency = convertedAmount
  )

  REVERTED_PURCHASE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_reward,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_reverted_mini
  )

  REVERTED_EXTRA_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_gift,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    subIcon = R.drawable.ic_transaction_reverted_mini,
    currency = convertedAmount
  )

  REVERTED_PROMO_CODE_BONUS -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_gift,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    subIcon = R.drawable.ic_transaction_reverted_mini,
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
    title = R.string.error_general,
    description = description,
    amount = mainAmount,
    currency = convertedAmount
  )

  REJECTED_E_SKILLS_TICKET -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_reverted_mini
  )

  FUNDS_SENT -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_transfer,
    title = R.string.title_send,
    amount = mainAmount,
    description = description,
    currency = convertedAmount
  )

  FUNDS_RECEIVED -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_transfer,
    title = R.string.error_general,
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
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_refund_mini
  )

  REJECTED_PURCHASE -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted_reward,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_reverted_mini
  )

  REVERTED_TOP_UP -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_reverted,
    title = R.string.error_general,
    amount = mainAmount,
    currency = convertedAmount,
  )

  REJECTED_TOP_UP -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_rejected_topup,
    title = R.string.error_general,
    amount = mainAmount,
    currency = convertedAmount,
  )

  SUBSCRIPTION_PAYMENT -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount
  )

  SUBSCRIPTION_REFUND -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_refund_mini
  )

  REJECTED_SUBSCRIPTION_PURCHASE -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
    subIcon = R.drawable.ic_transaction_reverted_mini
  )

  VOUCHER_PURCHASE -> TransactionCardInfo(
    appIcon = appIcon,
    title = R.string.error_general,
    amount = mainAmount,
    description = description,
    currency = convertedAmount,
  )

  else -> TransactionCardInfo(
    icon = R.drawable.ic_transaction_transfer,
    title = R.string.error_general,
    amount = mainAmount,
    currency = convertedAmount
  )
}