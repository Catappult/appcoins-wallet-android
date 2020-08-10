package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseListResponse
import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseResponse
import com.appcoins.wallet.bdsbilling.SubscriptionsResponse
import com.appcoins.wallet.bdsbilling.repository.entity.*

class SubscriptionsMapper {

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

  fun map(packageName: String,
          purchasesResponseSubscription: SubscriptionPurchaseListResponse): List<Purchase> {
    return purchasesResponseSubscription.items.map { map(packageName, it) }
  }

  fun map(packageName: String,
          subscriptionPurchaseResponse: SubscriptionPurchaseResponse): Purchase {
    return Purchase(subscriptionPurchaseResponse.uid,
        RemoteProduct(subscriptionPurchaseResponse.sku), subscriptionPurchaseResponse.status.name,
        subscriptionPurchaseResponse.state.name, subscriptionPurchaseResponse.autoRenewing,
        Package(packageName), Signature(subscriptionPurchaseResponse.verification.signature,
        subscriptionPurchaseResponse.verification.data))
  }
}
