package com.asfoundation.wallet.iab.domain.model

import java.math.BigDecimal
import kotlin.random.Random

data class ProductInfoData(
  val id: String,
  val packageName: String,
  val title: String,
  val description: String?,
  val transaction: TransactionPrice
)

data class TransactionPrice(
  val base: String?,
  val appcoinsAmount: Double,
  val amount: BigDecimal,
  val currency: String,
  val currencySymbol: String
)

val emptyProductInfoData = ProductInfoData(
  id = Random.nextInt(1, 1000000).toString(),
  packageName = emptyPurchaseData.domain,
  title = "Product Name",
  description = "Description",
  transaction = TransactionPrice(
    base = "",
    appcoinsAmount = 100.00,
    amount = BigDecimal(0.05),
    currency = "EUR",
    currencySymbol = "€"
  )
)
