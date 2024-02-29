package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.core.network.microservices.model.DetailsResponseBody
import com.appcoins.wallet.core.network.microservices.model.GetMethodsResponse
import com.appcoins.wallet.core.network.microservices.model.GetPurchasesResponse
import com.appcoins.wallet.core.network.microservices.model.InappPurchaseResponse
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.network.microservices.model.SubscriptionPurchaseListResponse
import com.appcoins.wallet.core.network.microservices.model.SubscriptionPurchaseResponse
import com.appcoins.wallet.core.network.microservices.model.SubscriptionsResponse

class BdsApiResponseMapper(
  private val subscriptionsMapper: SubscriptionsMapper,
  private val inAppMapper: InAppMapper
) {

  fun map(productDetails: DetailsResponseBody): List<Product> {
    return inAppMapper.map(productDetails)
  }

  fun map(packageName: String, inappPurchaseResponse: InappPurchaseResponse): Purchase {
    return inAppMapper.map(packageName, inappPurchaseResponse)
  }

  fun map(packageName: String, purchasesResponse: GetPurchasesResponse): List<Purchase> {
    return inAppMapper.map(packageName, purchasesResponse)
  }

  fun map(gatewaysResponse: GetMethodsResponse): List<PaymentMethodEntity> {
    return gatewaysResponse.items
  }

  fun map(subscriptionsResponse: SubscriptionsResponse): List<Product> {
    return subscriptionsMapper.map(subscriptionsResponse)
  }

  fun map(
    packageName: String,
    purchasesResponseSubscription: SubscriptionPurchaseListResponse
  ): List<Purchase> {
    return subscriptionsMapper.map(packageName, purchasesResponseSubscription)
  }

  fun map(
    packageName: String,
    subscriptionPurchaseResponse: SubscriptionPurchaseResponse
  ): Purchase {
    return subscriptionsMapper.map(packageName, subscriptionPurchaseResponse)
  }
}
