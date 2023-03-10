package com.appcoins.wallet.bdsbilling.repository.entity

abstract class Product(
  open val sku: String,
  open val title: String,
  open val description: String?,
  open val transactionPrice: TransactionPrice,
  open val billingType: String,
  open val subscriptionPeriod: String? = null, //Subs only
  open val trialPeriod: String? = null //Subs only
)

data class InAppProduct(
  override val sku: String,
  override val title: String,
  override val description: String?,
  override val transactionPrice: TransactionPrice,
  override val billingType: String
) :
  Product(sku, title, description, transactionPrice, billingType)

data class SubsProduct(
  override val sku: String,
  override val title: String,
  override val description: String,
  override val transactionPrice: TransactionPrice,
  override val billingType: String,
  override val subscriptionPeriod: String,
  override val trialPeriod: String?
) :
  Product(sku, title, description, transactionPrice, billingType, subscriptionPeriod, trialPeriod)

data class TransactionPrice(
  val base: String?,
  val appcoinsAmount: Double,
  val amount: Double,
  val currency: String,
  val currencySymbol: String
)
