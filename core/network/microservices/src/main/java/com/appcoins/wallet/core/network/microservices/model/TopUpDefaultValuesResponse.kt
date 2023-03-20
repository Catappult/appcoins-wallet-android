package com.appcoins.wallet.core.network.microservices.model

data class TopUpDefaultValuesResponse(val items: List<TopUpDefaultValueBody>) {

  data class TopUpDefaultValueBody(val uid: String, val label: String, val description: String,
                                   val price: Price)

  data class Price(val fiat: Fiat)

  data class Fiat(val value: String, val currency: Currency)

  data class Currency(val code: String, val sign: String)
}