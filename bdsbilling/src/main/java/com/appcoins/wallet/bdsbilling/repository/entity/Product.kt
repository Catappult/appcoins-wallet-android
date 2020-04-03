package com.appcoins.wallet.bdsbilling.repository.entity

data class Product(
    val sku: String,
    val title: String,
    val description: String,
    val price: Price,
    val billingType: String,
    val subscriptionPeriod: String?,
    val trialPeriod: String?,
    val introductoryPrice: Price?
)

data class Price(val base: String?,
                 val appcoinsAmount: Double,
                 val amount: Double,
                 val currency: String,
                 val currencySymbol: String)
