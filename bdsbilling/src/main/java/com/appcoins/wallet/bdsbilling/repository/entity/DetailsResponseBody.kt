package com.appcoins.wallet.bdsbilling.repository.entity

import java.math.BigDecimal

data class DetailsResponseBody(val items: List<ResponseProduct>) {
  data class ResponseProduct(
    val sku: String,
    val title: String,
    val description: String = "",
    val price: Price
  )

  data class Price(
    val currency: String,
    val value: BigDecimal,
    val label: String,
    val symbol: String,
    val micros: Long,
    val appc: AppcPrice
  )

  data class AppcPrice(val value: BigDecimal, val label: String, val micros: Long)
}

fun DetailsResponseBody.merge(other: DetailsResponseBody): DetailsResponseBody {
  (items as ArrayList).addAll(other.items)
  return this
}