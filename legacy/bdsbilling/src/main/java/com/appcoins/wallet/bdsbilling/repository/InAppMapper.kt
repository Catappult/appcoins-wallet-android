package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.*

class InAppMapper {
  fun map(productDetails: DetailsResponseBody): List<Product> =
    ArrayList(productDetails.items.map {
      InAppProduct(
        it.sku,
        it.title,
        it.description,
        TransactionPrice(
          it.price.currency,
          it.price.appc.value.toDouble(),
          it.price.value.toDouble(),
          it.price.currency,
          it.price.symbol
        ),
        BillingSupportedType.INAPP.name
      )
    })

  fun map(packageName: String, inAppPurchaseResponse: InappPurchaseResponse): Purchase = Purchase(
    inAppPurchaseResponse.uid,
    RemoteProduct(inAppPurchaseResponse.sku),
    mapPurchaseState(inAppPurchaseResponse.state),
    false,
    null,
    Package(packageName),
    Signature(inAppPurchaseResponse.verification.signature, inAppPurchaseResponse.verification.data)
  )

  private fun mapPurchaseState(state: PurchaseState): State {
    return when (state) {
      PurchaseState.CONSUMED -> State.CONSUMED
      PurchaseState.PENDING -> State.PENDING
      PurchaseState.ACKNOWLEDGED -> State.ACKNOWLEDGED
    }
  }

  fun map(packageName: String, purchasesResponse: GetPurchasesResponse): List<Purchase> =
    purchasesResponse.items.map { map(packageName, it) }
}
