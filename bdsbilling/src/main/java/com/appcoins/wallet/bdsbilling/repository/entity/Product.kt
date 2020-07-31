package com.appcoins.wallet.bdsbilling.repository.entity

data class Product(
    val sku: String,
    val title: String,
    val description: String,
    val price: Price,
    val billingType: String,
    val subscriptionPeriod: String? = null, //Subs only
    val trialPeriod: String? = null, //Subs only
    val introductoryPrice: Intro? = null //Subs only
)

data class Intro(val period: String, val cycles: Int, val price: Price)

data class Price(val base: String?,
                 val appcoinsAmount: Double,
                 val amount: Double,
                 val currency: String,
                 val currencySymbol: String)
