package com.asfoundation.wallet.transactions

import androidx.compose.ui.text.style.TextDecoration
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.BURN
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.ESKILLS_ENTRY_TICKET
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.ESKILLS_REWARD
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.ESKILLS_TICKET_REFUND
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.ESKILLS_WITHDRAW
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.EXTRA_BONUS
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.FEE
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.FUNDS_RECEIVED
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.FUNDS_SENT
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.GIFTCARD
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.INAPP_PURCHASE
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.PROMO_CODE_BONUS
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.PURCHASE_BONUS
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.PURCHASE_REFUND
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.REJECTED_ESKILLS_TICKET
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.REJECTED_PURCHASE
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.REJECTED_SUBSCRIPTION_PURCHASE
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.REJECTED_TOPUP
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.REVERTED_EXTRA_BONUS
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.REVERTED_PROMO_CODE_BONUS
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.REVERTED_PURCHASE_BONUS
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.REVERTED_TOPUP
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.SUBSCRIPTION_PAYMENT
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.SUBSCRIPTION_REFUND
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.TOPUP
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.VOUCHER_PURCHASE
import com.appcoins.wallet.core.network.backend.model.TransactionTypeResponse.WITHDRAW
import com.asf.wallet.R

data class TransactionCardInfo(
    val id: String? = null,
    val title: Int,
    val icon: Int? = null,
    val app: String? = null,
    val appIcon: String? = null,
    val description: String? = null,
    val amount: String? = null,
    val amountSubtitle: String? = null,
    val subIcon: Int? = null,
    val textDecoration: TextDecoration = TextDecoration.None,
    val status: StatusType,
    val date: String,
    val from: String? = null,
    val to: String? = null,
    val failedMessage: Int? = null,
    val sku: String? = null,
    val txId: String? = null
)

fun TransactionModel.cardInfoByType() =
    when (this.type) {
        PURCHASE_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reward,
                title = R.string.transaction_type_purchase_bonus,
                description = description,
                appIcon = appIcon,
                app = app,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                sku = sku,
                txId = txId
            )

        TOPUP ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_topup,
                title = R.string.transaction_type_topup,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                txId = txId
            )

        GIFTCARD ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_gift,
                title = R.string.transaction_type_gift_card,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
            )

        EXTRA_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_gift,
                title = R.string.transaction_type_extra_bonus,
                amount = amount,
                description = description,
                app = app,
                appIcon = appIcon,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                sku = sku,
                txId = txId,
            )

        PROMO_CODE_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_gift,
                title = R.string.transaction_type_promo_code_bonus,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
            )

        REVERTED_PURCHASE_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_reward,
                title = R.string.transaction_type_reverted_purchase_bonus,
                amount = amount,
                description = description,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                failedMessage = R.string.transaction_reverted_body,
                txId = txId
            )

        REVERTED_EXTRA_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_gift,
                title = R.string.transaction_type_reverted_extra_bonus,
                amount = amount,
                description = description,
                app = app,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                failedMessage = R.string.transaction_reverted_body,
                txId = txId
            )

        REVERTED_PROMO_CODE_BONUS ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_gift,
                title = R.string.transaction_type_reverted_promo_code_bonus,
                amount = amount,
                description = description,
                app = app,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                failedMessage = R.string.transaction_reverted_body
            )

        ESKILLS_REWARD ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reward,
                title = R.string.transaction_type_eskills_reward,
                description = description,
                appIcon = appIcon,
                app = app,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                sku = sku,
                txId = txId
            )

        ESKILLS_TICKET_REFUND ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_eskills_ticke_refund,
                description = description,
                app = app,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                failedMessage = R.string.transaction_reverted_body,
                txId = txId
            )

        REJECTED_ESKILLS_TICKET ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_rejected_eskills_ticket,
                amount = amount,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                failedMessage = R.string.transaction_rejected_body,
                subIcon = R.drawable.ic_transaction_rejected_mini,
                textDecoration = TextDecoration.LineThrough,
                txId = txId
            )

        FUNDS_SENT ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_transfer,
                title = R.string.transaction_type_funds_sent,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                to = to,
                from = from,
                txId = txId
            )

        FUNDS_RECEIVED ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_transfer,
                title = R.string.transaction_type_funds_received,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                from = from,
                to = to,
                txId = txId
            )

        INAPP_PURCHASE ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_iab,
                amount = amount,
                description = description,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                id = orderId,
                to = to,
                from = from,
                sku = sku,
                txId = txId
            )

        PURCHASE_REFUND ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_reward,
                title = R.string.transaction_type_reverted_purchase_title,
                amount = amount,
                description = description,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                failedMessage = R.string.transaction_reverted_body,
                sku = sku,
                txId = txId
            )

        REJECTED_PURCHASE ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted_reward,
                title = R.string.transaction_type_rejected_purchase,
                amount = amount,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                subIcon = R.drawable.ic_transaction_rejected_mini,
                textDecoration = TextDecoration.LineThrough,
                failedMessage = R.string.transaction_rejected_body,
                sku = sku,
                txId = txId
            )

        REVERTED_TOPUP ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_reverted,
                title = R.string.transaction_type_reverted_topup,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                failedMessage = R.string.transaction_reverted_body,
                txId = txId
            )

        REJECTED_TOPUP ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_rejected_topup,
                title = R.string.transaction_type_rejected_topup,
                amount = amount,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                textDecoration = TextDecoration.LineThrough,
                failedMessage = R.string.transaction_rejected_body,
                txId = txId
            )

        SUBSCRIPTION_PAYMENT ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_subscription_payment,
                amount = amount,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                id = orderId,
                to = to,
                from = from,
                sku = sku,
                txId = txId
            )

        SUBSCRIPTION_REFUND ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_refund_subscription,
                amount = amount,
                description = description,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                subIcon = R.drawable.ic_transaction_refund_reverted_mini,
                failedMessage = R.string.transaction_reverted_body,
                sku = sku,
                txId = txId
            )

        REJECTED_SUBSCRIPTION_PURCHASE ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_rejected_subscription_purchase,
                amount = amount,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                failedMessage = R.string.transaction_rejected_body,
                subIcon = R.drawable.ic_transaction_rejected_mini,
                textDecoration = TextDecoration.LineThrough,
                sku = sku,
                txId = txId
            )

        ESKILLS_ENTRY_TICKET ->
            TransactionCardInfo(
                appIcon = appIcon,
                title = R.string.transaction_type_eskills,
                amount = amount,
                description = description,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
                id = orderId,
                to = to,
                from = from,
                sku = sku,
                txId = txId
            )

        ESKILLS_WITHDRAW ->
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_eskill,
                title = R.string.e_skills_withdraw_title,
                amount = amount,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
            )

        FEE,
        BURN,
        WITHDRAW,
        VOUCHER_PURCHASE -> {
            TransactionCardInfo(
                icon = R.drawable.ic_transaction_fallback,
                title = R.string.subtitle_transaction_num,
                amount = amount,
                description = description,
                app = app,
                amountSubtitle = amountSubtitle,
                date = date,
                status = status,
            )
        }
    }
