package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.billing.repository.entity.Price
import com.appcoins.wallet.billing.repository.entity.Product

class BdsApiResponseMapper {
  fun map(productDetails: DetailsResponseBody): List<Product> {
    return ArrayList(productDetails.items.map {
      Product(it.name, it.label, it.description,
          Price(it.price.base, it.price.appc, it.price.fiat.value, it.price.fiat.currency.code,
              it.price.fiat.currency.symbol))
    })
  }

    fun map(productDetails: GetPackageResponse): Boolean = true

    fun map(purchasesResponse: GetPurchasesResponse): List<Purchase> {
        return purchasesResponse.items
    }

  fun map(gatewaysResponse: GetMethodsResponse): List<PaymentMethod> {
    return gatewaysResponse.items
  }

  fun map(it: Void): Boolean? {
    return true
  }
}
