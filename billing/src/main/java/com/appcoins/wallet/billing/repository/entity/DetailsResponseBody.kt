package com.appcoins.wallet.billing.repository.entity

data class DetailsResponseBody(val items: List<ResponseProduct>) {
  data class ResponseProduct(val name: String, val label: String,
                             val description: String,
                             val `package`: PackageResponse,
                             val price: Price)

  data class Price(val appc: Double, val fiat: Fiat)
  data class Fiat(val value: String, val currency: Currency)
  data class Currency(val code: String, val symbol: String)

  data class PackageResponse(val name: String)
}