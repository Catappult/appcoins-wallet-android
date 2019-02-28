package com.appcoins.wallet.billing.repository.entity

class Product(val sku: String, val title: String, val description: String,
              val price: Price)

data class Price(val base: String?, val appcoinsAmount: Double, val amount: Double, val currency: String,
                 val currencySymbol: String)
