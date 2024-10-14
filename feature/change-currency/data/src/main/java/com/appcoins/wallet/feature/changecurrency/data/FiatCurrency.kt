package com.appcoins.wallet.feature.changecurrency.data

data class FiatCurrency(
  val currency: String,
  val flag: String?,
  val label: String?,
  val sign: String?
)
