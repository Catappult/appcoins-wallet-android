package com.asfoundation.wallet.iab.domain.model

data class ProductInfoData(
  val id: String,
  val title: String,
  val description: String?,
  val transaction: TransactionPrice
)

data class TransactionPrice(
  val base: String?,
  val appcoinsAmount: Double,
  val amount: Double,
  val currency: String,
  val currencySymbol: String
)
