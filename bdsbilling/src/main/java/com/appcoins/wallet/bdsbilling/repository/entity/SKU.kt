package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.annotations.SerializedName

class SKU(val productId: String, val type: String, val price: String,
          @field:SerializedName("price_currency") val currency: String,
          @field:SerializedName("price_amount_micros") val amount: Int,
          val title: String, val description: String)
