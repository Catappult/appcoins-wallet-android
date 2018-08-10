package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.repository.entity.*

class BdsApiResponseMapper {
  fun map(productDetails: DetailsResponseBody): List<Product> {
    return ArrayList(productDetails.items.map {
      Product(it.name, it.label, it.description,
          Price(it.price.appc, it.price.fiat.value, it.price.fiat.currency.code,
              it.price.fiat.currency.symbol))
    })
  }

    fun map(productDetails: GetPackageResponse): Boolean = true

    fun map(purchasesResponse: GetPurchasesResponse): List<Purchase> {
        return purchasesResponse.items
    }

  fun map(gatewaysResponse: GetGatewaysResponse): List<Gateway> {
    return gatewaysResponse.items
  }

  fun map(it: Void): Boolean? {
    return true
  }
}
