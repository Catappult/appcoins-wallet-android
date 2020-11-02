package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.entity.*

class InAppMapper(private val serializer: ExternalBillingSerializer) {
  fun map(productDetails: DetailsResponseBody): List<Product> {
    return ArrayList(productDetails.items.map {
      InAppProduct(it.name, it.label, it.description,
          Price(it.price.base, it.price.appc, it.price.fiat.value, it.price.fiat.currency.code,
              it.price.fiat.currency.symbol), BillingSupportedType.INAPP.name)
    })
  }

  fun map(packageName: String, inAppPurchaseResponse: InappPurchaseResponse): Purchase {
    val signatureEntity = inAppPurchaseResponse.signature
    val signatureMessage = serializer.serializeSignatureData(inAppPurchaseResponse)
    return Purchase(inAppPurchaseResponse.uid,
        RemoteProduct(inAppPurchaseResponse.product.name), inAppPurchaseResponse.status, null,
        false,
        Package(packageName), Signature(signatureEntity.value, signatureMessage))
  }

  fun map(packageName: String, purchasesResponse: GetPurchasesResponse): List<Purchase> {
    return purchasesResponse.items.map { map(packageName, it) }
  }
}
