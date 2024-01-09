package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.core.network.microservices.model.*

class BdsApiResponseMapper(private val subscriptionsMapper: SubscriptionsMapper,
                           private val inAppMapper: InAppMapper) {

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

    //return gatewaysResponse.items    // TODO uncomment and remove the rest. for testing without MS integration

    val items = mutableListOf<PaymentMethodEntity>()
    items.addAll(gatewaysResponse.items)
    //adds the google pay method to the list
    val googlePay = PaymentMethodEntity(
      "googlepay",
      "Google Pay (Mock)",
      "https://play-lh.googleusercontent.com/Q6_GqKzmB3y_p7iEK1xY4hxMQ9TCnO08HerDUpaGUHXR6Bplyfv5Z97Kri51cJPWG_i1=w240-h480-rw",
      "AVAILABLE",
      Gateway(Gateway.Name.adyen_v2,"",""),
      false,
      null
    )
    items.add(googlePay)
    return items
  }

  fun map(subscriptionsResponse: SubscriptionsResponse): List<Product> {
    return subscriptionsMapper.map(subscriptionsResponse)
  }

  fun map(packageName: String,
          purchasesResponseSubscription: SubscriptionPurchaseListResponse): List<Purchase> {
    return subscriptionsMapper.map(packageName, purchasesResponseSubscription)
  }

  fun map(packageName: String,
          subscriptionPurchaseResponse: SubscriptionPurchaseResponse): Purchase {
    return subscriptionsMapper.map(packageName, subscriptionPurchaseResponse)
  }
}
