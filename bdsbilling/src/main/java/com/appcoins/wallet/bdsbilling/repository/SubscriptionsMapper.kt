package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.PurchaseState
import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseListResponse
import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseResponse
import com.appcoins.wallet.bdsbilling.SubscriptionsResponse
import com.appcoins.wallet.bdsbilling.repository.entity.*

class SubscriptionsMapper {

  fun map(subscriptionsResponse: SubscriptionsResponse): List<Product> {
    return ArrayList(subscriptionsResponse.items.map {
      SubsProduct(it.sku, it.title, it.description,
          Price(it.price.currency, it.price.appc.value.toDouble(), it.price.value.toDouble(),
              it.price.currency, it.price.symbol), BillingSupportedType.INAPP_SUBSCRIPTION.name,
          it.period, it.trialPeriod)
    })
  }

  fun map(packageName: String,
          purchasesResponseSubscription: SubscriptionPurchaseListResponse): List<Purchase> {
    return purchasesResponseSubscription.items.map { map(packageName, it) }
  }

  fun map(packageName: String,
          subscriptionPurchaseResponse: SubscriptionPurchaseResponse): Purchase {
    return Purchase(subscriptionPurchaseResponse.uid,
        RemoteProduct(subscriptionPurchaseResponse.sku),
        mapPurchaseState(subscriptionPurchaseResponse.state),
        subscriptionPurchaseResponse.autoRenewing, subscriptionPurchaseResponse.renewal,
        Package(packageName), Signature(subscriptionPurchaseResponse.verification.signature,
        subscriptionPurchaseResponse.verification.data))
  }

  private fun mapPurchaseState(state: PurchaseState): State {
    return when (state) {
      PurchaseState.CONSUMED -> State.CONSUMED
      PurchaseState.PENDING -> State.PENDING
      PurchaseState.ACKNOWLEDGED -> State.ACKNOWLEDGED
    }
  }
}
