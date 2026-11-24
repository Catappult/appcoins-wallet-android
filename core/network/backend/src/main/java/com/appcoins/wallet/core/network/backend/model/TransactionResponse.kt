package com.appcoins.wallet.core.network.backend.model

import com.fasterxml.jackson.annotation.JsonProperty

const val POSITIVE_SIGN = "+"
const val NEGATIVE_SIGN = "-"

data class TransactionResponse(
  @JsonProperty("txid") val txId: String?,
  @JsonProperty("status") val status: StatusResponse,
  @JsonProperty("type") val type: TransactionTypeResponse,
  @JsonProperty("sender") val sender: String,
  @JsonProperty("receiver") val receiver: String,
  @JsonProperty("amount") val amount: String,
  @JsonProperty("amount_currency") val amountCurrency: String,
  @JsonProperty("ts") val ts: String,
  @JsonProperty("processed_time") val processedTime: String,
  @JsonProperty("app") val app: String?,
  @JsonProperty("app_icon") val appIcon: String?,
  @JsonProperty("bonus") val bonus: Float?,
  @JsonProperty("bonus_description") val bonusDescription: String?,
  @JsonProperty("method") val method: String?,
  @JsonProperty("paid_currency") val paidCurrency: String?,
  @JsonProperty("paid_currency_amount") val paidCurrencyAmount: String?,
  @JsonProperty("default_currency_amount") val defaultCurrencyAmount: String?,
  @JsonProperty("reference") val reference: String?,
  @JsonProperty("refund_txid") val refundTxId: String?,
  @JsonProperty("revert_txid") val revertTxId: String?,
  @JsonProperty("parent_txid") val parentTxId: String?,
  @JsonProperty("sku") val sku: String?,
  @JsonProperty("invoice_id") val invoiceId: String?,
  @JsonProperty("partial_credits_payment") val partialCreditsPayment: PartialCreditsPaymentResponse?
)

/**
 * The AppCoins Credits funded slice of a partial payment: the buyer spent part of their balance
 * and the remainder was charged to [TransactionResponse.method].
 *
 * [TransactionResponse.amount] and [TransactionResponse.paidCurrencyAmount] stay the purchase
 * total; this is the split beneath them. Served only on transactions that were part-funded from
 * the balance, `null` otherwise.
 */
data class PartialCreditsPaymentResponse(
  // wei, same unit as TransactionResponse.amount
  @JsonProperty("appc_amount") val appcAmount: String,
  @JsonProperty("usd_amount") val usdAmount: String?,
  // the credits slice, in TransactionResponse.paidCurrency
  @JsonProperty("paid_currency_amount") val paidCurrencyAmount: String,
  // total minus the slice - what `method` took. Null only when the row has no total to subtract from
  @JsonProperty("charged_amount") val chargedAmount: String?
)

data class InvoiceResponse(@JsonProperty("url") val url: String)

enum class StatusResponse {
  SUCCESS,
  REJECTED,
  PENDING
}

enum class TransactionTypeResponse(val sign: String = "") {
  TOPUP(sign = POSITIVE_SIGN),
  GIFTCARD(sign = POSITIVE_SIGN),
  PURCHASE_BONUS(sign = POSITIVE_SIGN),
  EXTRA_BONUS(sign = POSITIVE_SIGN),
  PROMO_CODE_BONUS(sign = POSITIVE_SIGN),
  ESKILLS_REWARD(sign = POSITIVE_SIGN),
  ESKILLS_TICKET_REFUND(sign = POSITIVE_SIGN),
  FUNDS_RECEIVED(sign = POSITIVE_SIGN),
  PURCHASE_REFUND(sign = POSITIVE_SIGN),
  SUBSCRIPTION_REFUND(sign = POSITIVE_SIGN),
  CHALLENGE_REWARD(sign = POSITIVE_SIGN),
  REJECTED_TOPUP(sign = POSITIVE_SIGN),
  REJECTED_PURCHASE(sign = POSITIVE_SIGN),
  REJECTED_ESKILLS_TICKET(sign = POSITIVE_SIGN),
  REJECTED_SUBSCRIPTION_PURCHASE(sign = POSITIVE_SIGN),
  REJECTED_CHALLENGE_REWARD(sign = POSITIVE_SIGN),
  SUBSCRIPTION_PAYMENT(sign = NEGATIVE_SIGN),
  REVERTED_PURCHASE_BONUS(sign = NEGATIVE_SIGN),
  REVERTED_EXTRA_BONUS(sign = NEGATIVE_SIGN),
  REVERTED_PROMO_CODE_BONUS(sign = NEGATIVE_SIGN),
  REVERTED_CHALLENGE_REWARD(sign = NEGATIVE_SIGN),
  ESKILLS_ENTRY_TICKET(sign = NEGATIVE_SIGN),
  FUNDS_SENT(sign = NEGATIVE_SIGN),
  INAPP_PURCHASE(sign = NEGATIVE_SIGN),
  REVERTED_TOPUP(sign = NEGATIVE_SIGN),
  ESKILLS_WITHDRAW(sign = NEGATIVE_SIGN),
  BURN,
  FEE,
  WITHDRAW,
  VOUCHER_PURCHASE
}
