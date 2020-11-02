package com.appcoins.wallet.bdsbilling.repository.entity

abstract class Product(open val sku: String,
                       open val title: String,
                       open val description: String,
                       open val price: Price,
                       open val billingType: String,
                       open val subscriptionPeriod: String? = null, //Subs only
                       open val trialPeriod: String? = null //Subs only
)

data class InAppProduct(override val sku: String,
                        override val title: String,
                        override val description: String,
                        override val price: Price,
                        override val billingType: String) :
    Product(sku, title, description, price, billingType)

data class SubsProduct(override val sku: String,
                       override val title: String,
                       override val description: String,
                       override val price: Price,
                       override val billingType: String,
                       override val subscriptionPeriod: String,
                       override val trialPeriod: String?) :
    Product(sku, title, description, price, billingType, subscriptionPeriod, trialPeriod)

class Price(val base: String?,
            val appcoinsAmount: Double,
            val amount: Double,
            val currency: String,
            val currencySymbol: String)
