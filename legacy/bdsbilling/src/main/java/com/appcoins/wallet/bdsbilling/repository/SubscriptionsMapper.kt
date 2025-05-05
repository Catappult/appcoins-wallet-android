package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.Package
import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.RemoteProduct
import com.appcoins.wallet.bdsbilling.repository.entity.Signature
import com.appcoins.wallet.bdsbilling.repository.entity.State
import com.appcoins.wallet.bdsbilling.repository.entity.SubsProduct
import com.appcoins.wallet.bdsbilling.repository.entity.TransactionPrice
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.network.microservices.model.PurchaseState
import com.appcoins.wallet.core.network.microservices.model.SubscriptionPurchaseListResponse
import com.appcoins.wallet.core.network.microservices.model.SubscriptionPurchaseResponse
import com.appcoins.wallet.core.network.microservices.model.SubscriptionsResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubscriptionsMapper {

  fun map(subscriptionsResponse: SubscriptionsResponse): List<Product> {
    return ArrayList(subscriptionsResponse.items.map {
      SubsProduct(
        sku = it.sku,
        title = it.title,
        description = it.description,
        transactionPrice = TransactionPrice(
          base = it.subscriptionPrice.currency,
          appcoinsAmount = it.subscriptionPrice.appc.value.toDouble(),
          amount = it.subscriptionPrice.value.toDouble(),
          currency = it.subscriptionPrice.currency,
          currencySymbol = it.subscriptionPrice.symbol
        ),
        billingType = BillingSupportedType.SUBS_TYPE,
        subscriptionPeriod = it.period,
        trialPeriod = it.trialPeriod,
        freeTrialDuration = it.subscriptionPrice.trial?.freeTrialDuration,
        subscriptionStartingDate = it.subscriptionPrice.trial?.subscriptionStartingDate
      )
    })
  }

  fun map(
    packageName: String,
    purchasesResponseSubscription: SubscriptionPurchaseListResponse
  ): List<Purchase> {
    return purchasesResponseSubscription.items.map { map(packageName, it) }
  }

  fun map(
    packageName: String,
    subscriptionPurchaseResponse: SubscriptionPurchaseResponse
  ): Purchase {
    return Purchase(
      subscriptionPurchaseResponse.uid,
      RemoteProduct(subscriptionPurchaseResponse.sku),
      mapPurchaseState(subscriptionPurchaseResponse.state),
      subscriptionPurchaseResponse.autoRenewing,
      mapRenewalDate(subscriptionPurchaseResponse.renewal),
      Package(packageName), Signature(
        subscriptionPurchaseResponse.verification.signature,
        subscriptionPurchaseResponse.verification.data
      )
    )
  }

  private fun mapRenewalDate(renewal: String?): Date? {
    return if (renewal == null) null
    else {
      val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
      dateFormat.parse(renewal)
    }
  }

  private fun mapPurchaseState(state: PurchaseState): State {
    return when (state) {
      PurchaseState.CONSUMED -> State.CONSUMED
      PurchaseState.PENDING -> State.PENDING
      PurchaseState.ACKNOWLEDGED -> State.ACKNOWLEDGED
    }
  }
}
