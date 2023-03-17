package com.appcoins.wallet.core.network.microservices.model

import java.math.BigDecimal

data class ConversionResponseBody(
  val currency: String,
  val value: BigDecimal,
  val label: String,
  val sign: String
)