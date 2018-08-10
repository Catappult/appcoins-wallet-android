package com.appcoins.wallet.billing.repository.entity

class Product(val sku: String, val title: String, val description: String,
              val price: Price)

data class Price(val appcoinsAmount: Double, val amount: String, val currency: String,
                 val currencySymbol: String)
