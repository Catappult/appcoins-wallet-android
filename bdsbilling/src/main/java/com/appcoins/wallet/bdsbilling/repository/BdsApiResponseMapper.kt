package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseListResponse
import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseResponse
import com.appcoins.wallet.bdsbilling.SubscriptionsResponse
import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson

class BdsApiResponseMapper {

  fun map(productDetails: DetailsResponseBody): List<Product> {
    return ArrayList(productDetails.items.map {
      Product(it.name, it.label, it.description,
          Price(it.price.base, it.price.appc, it.price.fiat.value, it.price.fiat.currency.code,
              it.price.fiat.currency.symbol), BillingSupportedType.INAPP.name)
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
      val intro = it.intro?.let { intro ->
        Intro(intro.period, intro.cycles,
            Price(intro.price.currency, intro.price.appc.value.toDouble(),
                intro.price.value.toDouble(),
                intro.price.currency, intro.price.currency))
      }
      Product(it.sku, it.title, it.description,
          Price(it.price.currency, it.price.appc.value.toDouble(), it.price.value.toDouble(),
              it.price.currency, it.price.currency), BillingSupportedType.INAPP_SUBSCRIPTION.name,
          it.period, it.trialPeriod,
          intro
      )
    })
  }

  //TODO this method should be in SubscriptionsResponse mapper
  fun map(packageName: String,
          purchasesResponseSubscription: SubscriptionPurchaseListResponse): List<Purchase> {
    return purchasesResponseSubscription.items.map { map(packageName, it) }
  }

  //TODO this method should be in SubscriptionsResponse mapper
  fun map(packageName: String,
          subscriptionPurchaseResponse: SubscriptionPurchaseResponse): Purchase {
    val objectMapper = ObjectMapper()
    val developerPurchase =
        objectMapper.readValue(Gson().toJson(subscriptionPurchaseResponse.verification.data),
            DeveloperPurchase::class.java)

    return Purchase(subscriptionPurchaseResponse.uid,
        RemoteProduct(subscriptionPurchaseResponse.sku), subscriptionPurchaseResponse.status.name,
        subscriptionPurchaseResponse.autoRenewing, Package(packageName),
        Signature(subscriptionPurchaseResponse.verification.signature, developerPurchase))
  }
}
