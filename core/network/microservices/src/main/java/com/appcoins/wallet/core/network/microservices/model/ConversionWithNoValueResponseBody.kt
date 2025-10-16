package com.appcoins.wallet.core.network.microservices.model

data class ConversionWithNoValueResponseBody(
  val currency: String,
  val label: String,
  val sign: String
)