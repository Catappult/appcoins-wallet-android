package com.appcoins.wallet.bdsbilling.repository.entity

data class Product(
    val sku: String,
    val title: String,
    val description: String,
    val price: Price,
    val billingType: String,
    val subscriptionPeriod: String? = null,
    val trialPeriod: String? = null,
    val introductoryPrice: Price? = null
)

data class Price(val base: String?,
                 val appcoinsAmount: Double,
                 val amount: Double,
                 val currency: String,
                 val currencySymbol: String)
