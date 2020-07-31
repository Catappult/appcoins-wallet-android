package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.DetailsResponseBody
import com.appcoins.wallet.bdsbilling.repository.entity.Price
import com.appcoins.wallet.bdsbilling.repository.entity.Product

class InAppMapper {
  fun map(productDetails: DetailsResponseBody): List<Product> {
    return ArrayList(productDetails.items.map {
      Product(it.name, it.label, it.description,
          Price(it.price.base, it.price.appc, it.price.fiat.value, it.price.fiat.currency.code,
              it.price.fiat.currency.symbol), BillingSupportedType.INAPP.name)
    })
  }

}
