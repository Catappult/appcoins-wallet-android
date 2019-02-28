package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.annotations.SerializedName

class SKU(val productId: String, val type: String,
          @field:SerializedName("price") val priceBase: String,
          @field:SerializedName("price_currency_code") val currencyBase: String,
          @field:SerializedName("price_amount_micros") val amountBase: Int,
          @field:SerializedName("appc_price") val priceAppc: String,
          @field:SerializedName("appc_price_currency_code") val currencyAppc: String,
          @field:SerializedName("appc_price_amount_micros") val amountAppc: Int,
          @field:SerializedName("fiat_price") val priceFiat: String,
          @field:SerializedName("fiat_price_currency_code") val currencyFiat: String,
          @field:SerializedName("fiat_price_amount_micros") val amountFiat: Int,
          val title: String, val description: String)
