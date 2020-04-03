package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.PurchaseResponse
import com.appcoins.wallet.bdsbilling.SubscriptionsResponse
import com.appcoins.wallet.bdsbilling.repository.entity.*

class BdsApiResponseMapper {
  fun map(productDetails: DetailsResponseBody): List<Product> {
    return ArrayList(productDetails.items.map {
      Product(it.name, it.label, it.description,
          Price(it.price.base, it.price.appc, it.price.fiat.value, it.price.fiat.currency.code,
              it.price.fiat.currency.symbol), "inapp", null, null, null)
    })
  }

  fun map(purchasesResponse: GetPurchasesResponse): List<Purchase> {
    return purchasesResponse.items
  }

  fun map(gatewaysResponse: GetMethodsResponse): List<PaymentMethodEntity> {
    return gatewaysResponse.items
  }

  //TODO this method should be in SubscriptionsResponse mapper
  //TODO current price values are wrong
  //TODO Price has currency but we need code and symbol
  fun map(subscriptionsResponse: SubscriptionsResponse): List<Product> {
    return ArrayList(subscriptionsResponse.items.map {
      Product(it.sku, it.title, it.description,
          Price(it.price.currency, it.price.appc.value.toDouble(), it.price.value.toDouble(),
              it.price.currency, it.price.currency), "subs", it.period, it.trialPeriod,
          Price(it.intro.price.currency, it.intro.price.appc.value.toDouble(),
              it.intro.price.value.toDouble(),
              it.intro.price.currency, it.intro.price.currency)
      )
    })
  }

  //TODO this method should be in SubscriptionsResponse mapper
  //TODO This method does nothing. Needs to me implemented
  fun map(packageName: String, purchasesResponse: PurchaseResponse): List<Purchase> {
    return purchasesResponse.items.map { map(packageName, it) }
  }

  //TODO this method should be in SubscriptionsResponse mapper
  //TODO This method does nothing. Needs to me implemented
  fun map(packageName: String, purchase: com.appcoins.wallet.bdsbilling.Purchase): Purchase {
    return Purchase(purchase.uid, RemoteProduct(purchase.sku), purchase.status.name.toString(),
        Package(packageName), Signature("", DeveloperPurchase()))
  }
}
