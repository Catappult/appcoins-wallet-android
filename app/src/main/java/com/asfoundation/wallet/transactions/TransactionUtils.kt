package com.asfoundation.wallet.transactions

import androidx.compose.ui.text.style.TextDecoration
import com.asf.wallet.R
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

data class TransactionCardInfo(
    val id: String? = null,
    val title: Int,
    val icon: Int? = null,
    val app: String? = null,
    val appIcon: String? = null,
    val description: String? = null,
    val amount: String? = null,
    val convertedAmount: String? = null,
    val subIcon: Int? = null,
    val textDecoration: TextDecoration = TextDecoration.None,
    val status: StatusType,
    val date: String,
    val from: String? = null,
    val to: String? = null,
    val transactionUrl: String,
    val failedMessage: Int? = null
)

fun TransactionModel.cardInfoByType() =
    when (this.type) {
        PURCHASE_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reward,
                title = R.string.transaction_type_purchase_bonus,
                description = description,
                app = app,
                amount = mainAmount,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                id = id
            )

        TOP_UP ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_topup,
                title = R.string.transaction_type_topup,
                amount = mainAmount,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl
            )

        GIFT_CARD ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_gift,
                title = R.string.transaction_type_gift_card,
                amount = mainAmount,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl
            )

        EXTRA_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_gift,
                title = R.string.transaction_type_extra_bonus,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl
            )

        PROMO_CODE_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_gift,
                title = R.string.transaction_type_promo_code_bonus,
                amount = mainAmount,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl
            )

        REVERTED_PURCHASE_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_reward,
                title = R.string.transaction_type_reverted_purchase_bonus,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                failedMessage = R.string.transaction_reverted_body,
                id = id
            )

        REVERTED_EXTRA_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_gift,
                title = R.string.transaction_type_reverted_extra_bonus,
                amount = mainAmount,
                description = description,
                app = app,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                failedMessage = R.string.transaction_reverted_body
            )

        REVERTED_PROMO_CODE_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_gift,
                title = R.string.transaction_type_reverted_promo_code_bonus,
                amount = mainAmount,
                description = description,
                app = app,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                failedMessage = R.string.transaction_reverted_body
            )

        E_SKILLS_REWARD ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reward,
                title = R.string.transaction_type_eskills_reward,
                description = description,
                app = app,
                amount = mainAmount,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl
            )

        E_SKILLS_TICKET_REFUND ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_eskills_ticke_refund,
                description = description,
                app = app,
                amount = mainAmount,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                failedMessage = R.string.transaction_reverted_body
            )

        REJECTED_E_SKILLS_TICKET ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_rejected_eskills_ticket,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                failedMessage = R.string.transaction_rejected_body,
                subIcon = R.drawable.ic_transaction_rejected_mini,
                textDecoration = TextDecoration.LineThrough
            )

        FUNDS_SENT ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_transfer,
                title = R.string.transaction_type_funds_sent,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                to = to
            )

        FUNDS_RECEIVED ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_transfer,
                title = R.string.transaction_type_funds_received,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                from = from
            )

        IN_APP_PURCHASE ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_iab,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                id = id,
                to = to
            )

        PURCHASE_REFUND ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_reward,
                title = R.string.transaction_type_reverted_purchase_title,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                failedMessage = R.string.transaction_reverted_body,
                id = id,
                to = to
            )

        REJECTED_PURCHASE ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_reward,
                title = R.string.transaction_type_rejected_purchase,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                subIcon = R.drawable.ic_transaction_rejected_mini,
                textDecoration = TextDecoration.LineThrough,
                failedMessage = R.string.transaction_rejected_body,
                id = id,
                to = to
            )

        REVERTED_TOP_UP ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted,
                title = R.string.transaction_type_reverted_topup,
                amount = mainAmount,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                failedMessage = R.string.transaction_reverted_body
            )

        REJECTED_TOP_UP ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_rejected_topup,
                title = R.string.transaction_type_rejected_topup,
                amount = mainAmount,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                textDecoration = TextDecoration.LineThrough,
                failedMessage = R.string.transaction_rejected_body
            )

        SUBSCRIPTION_PAYMENT ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_subscription_payment,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl
            )

        SUBSCRIPTION_REFUND ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_refund_subscription,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                failedMessage = R.string.transaction_reverted_body
            )

        REJECTED_SUBSCRIPTION_PURCHASE ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_rejected_subscription_purchase,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
                failedMessage = R.string.transaction_rejected_body,
                subIcon = R.drawable.ic_transaction_rejected_mini,
                textDecoration = TextDecoration.LineThrough,
                id = id,
                to = to
            )

        E_SKILLS_ENTRY_TICKET ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_eskills,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
            )

        OTHER ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_transfer,
                title = R.string.error_general,
                amount = mainAmount,
                description = description,
                app = app,
                convertedAmount = convertedAmount,
                date = date,
                status = status,
                transactionUrl = transactionUrl,
            )
    }
