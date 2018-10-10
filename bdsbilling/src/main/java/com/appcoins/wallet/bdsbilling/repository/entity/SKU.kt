package com.appcoins.wallet.billing.repository.entity

import com.google.gson.annotations.SerializedName

class SKU(val productId: String, val type: String, val price: String,
          @field:SerializedName("price_currency_code") val currency: String,
          @field:SerializedName("price_amount_micros") val amount: Double,
          val title: String, val description: String)
