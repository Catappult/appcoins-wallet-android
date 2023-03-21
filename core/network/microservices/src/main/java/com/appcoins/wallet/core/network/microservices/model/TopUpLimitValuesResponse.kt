package com.appcoins.wallet.core.network.microservices.model


data class TopUpLimitValuesResponse(val currency: Currency, val values: Values) {

  data class Currency(val code: String, val sign: String)
  data class Values(val min: String, val max: String)
}