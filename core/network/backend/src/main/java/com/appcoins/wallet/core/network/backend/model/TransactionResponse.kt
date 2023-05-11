package com.appcoins.wallet.core.network.backend.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TransactionResponse(
  @JsonProperty("txid") val txId: String,
  @JsonProperty("status") val status: String,
  @JsonProperty("type") val type: String,
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
  @JsonProperty("parent_txid") val parentTxId: String?
)
