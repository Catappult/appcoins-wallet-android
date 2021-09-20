package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.entity.*

class InAppMapper(private val serializer: ExternalBillingSerializer) {
  fun map(productDetails: DetailsResponseBody): List<Product> {
    return ArrayList(productDetails.items.map {
      InAppProduct(it.name, it.label, it.description,
          TransactionPrice(it.price.base, it.price.appc, it.price.fiat.value, it.price.fiat.currency.code,
              it.price.fiat.currency.symbol), BillingSupportedType.INAPP.name)
    })
  }

  //As of right now any purchase that is returned in the inapp endpoint is not consumed and since
  // inapp purchase can't be ACKNOWLEDGE, to avoid null checks in the end the state is set as pending
  fun map(packageName: String, inAppPurchaseResponse: InappPurchaseResponse): Purchase {
    val signatureEntity = inAppPurchaseResponse.signature
    val signatureMessage = serializer.serializeSignatureData(inAppPurchaseResponse)
    return Purchase(inAppPurchaseResponse.uid, RemoteProduct(inAppPurchaseResponse.product.name),
        State.PENDING, false, null, Package(packageName),
        Signature(signatureEntity.value, signatureMessage))
  }

  fun map(packageName: String, purchasesResponse: GetPurchasesResponse): List<Purchase> {
    return purchasesResponse.items.map { map(packageName, it) }
  }
}
