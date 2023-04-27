package com.asfoundation.wallet.transactions

import com.appcoins.wallet.core.network.backend.model.TransactionResponse
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils

data class TransactionModel(
  val id: String,
  val type: String,
  val date: String,
  val mainAmount: String,
  val mainCurrency: String,
  val convertedAmount: String,
  val convertedCurrency: String
)


fun TransactionResponse.toModel() = TransactionModel(
  id = txId,
  type = type,
  date = DateFormatterUtils.getDate(processedTime),
  mainAmount = defaultCurrencyAmount ?: paidCurrencyAmount ?: amount,
  mainCurrency = paidCurrency ?: "",
  convertedAmount = if (defaultCurrencyAmount == null && paidCurrencyAmount == null) "" else amount,
  convertedCurrency = amountCurrency
)

