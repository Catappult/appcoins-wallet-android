package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.SubscriptionPurchasListResponse
import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseResponse
import com.appcoins.wallet.bdsbilling.SubscriptionsResponse
import com.appcoins.wallet.bdsbilling.repository.entity.*
import java.util.*
import kotlin.collections.ArrayList

class BdsApiResponseMapper {
  fun map(productDetails: DetailsResponseBody): List<Product> {
    return ArrayList(productDetails.items.map {
      Product(it.name, it.label, it.description,
          Price(it.price.base, it.price.appc, it.price.fiat.value, it.price.fiat.currency.code,
              it.price.fiat.currency.symbol), "inapp") //TODO replace hardcoded
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
      val introPrice = it.intro?.let { intro ->
        Price(intro.price.currency, intro.price.appc.value.toDouble(), intro.price.value.toDouble(),
            intro.price.currency, intro.price.currency)
      }
      Product(it.sku, it.title, it.description,
          Price(it.price.currency, it.price.appc.value.toDouble(), it.price.value.toDouble(),
              it.price.currency, it.price.currency), "subs", it.period, it.trialPeriod,
          introPrice
      )
    })
  }

  //TODO this method should be in SubscriptionsResponse mapper
  //TODO This method does nothing. Needs to me implemented
  fun map(packageName: String,
          purchasesResponseSubscription: SubscriptionPurchasListResponse): List<Purchase> {
    return purchasesResponseSubscription.items.map { map(packageName, it) }
  }

  //TODO this method should be in SubscriptionsResponse mapper
  //TODO This method does nothing. Needs to me implemented
  fun map(packageName: String,
          subscriptionPurchaseResponse: SubscriptionPurchaseResponse): Purchase {
    val developerPurchase = DeveloperPurchase()
    developerPurchase.packageName = packageName
    developerPurchase.orderId = subscriptionPurchaseResponse.orderReference
    developerPurchase.productId = subscriptionPurchaseResponse.sku
    developerPurchase.purchaseState = 0
    developerPurchase.purchaseTime = System.currentTimeMillis()
    developerPurchase.purchaseToken = UUID.randomUUID()
        .toString()
    return Purchase(subscriptionPurchaseResponse.uid,
        RemoteProduct(subscriptionPurchaseResponse.sku), subscriptionPurchaseResponse.status.name,
        subscriptionPurchaseResponse.autoRenewing, Package(packageName),
        Signature(subscriptionPurchaseResponse.verification.signature, developerPurchase))
  }
}
