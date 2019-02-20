package com.appcoins.wallet.bdsbilling.repository.entity

data class DetailsResponseBody(val items: List<ResponseProduct>) {
  data class ResponseProduct(val name: String, val label: String,
                             val description: String,
                             val `package`: PackageResponse,
                             val price: Price)

  data class Price(val base: String?, val appc: Double, val fiat: Fiat)
  data class Fiat(val value: Double, val currency: Currency)
  data class Currency(val code: String, val symbol: String)

  data class PackageResponse(val name: String)
}