package com.appcoins.wallet.billing.repository

import com.appcoins.wallet.billing.repository.entity.DetailsResponseBody
import com.appcoins.wallet.billing.repository.entity.GetPackageResponse
import com.appcoins.wallet.billing.repository.entity.Price
import com.appcoins.wallet.billing.repository.entity.Product

class BdsApiResponseMapper {
  fun map(productDetails: DetailsResponseBody): List<Product> {
    return ArrayList(productDetails.items.map {
      Product(it.name, it.label, it.description,
          Price(it.price.appc, it.price.fiat.value, it.price.fiat.currency.code,
              it.price.fiat.currency.symbol))
    })
  }

  fun map(productDetails: GetPackageResponse): Boolean = true
}
