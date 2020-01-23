package com.asfoundation.wallet.subscriptions

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Single
import java.math.BigDecimal

class SubscriptionRepository(
    private val subscriptionApi: SubscriptionService,//TODO unused until microservices are ready
    private val findDefaultWalletInteract: FindDefaultWalletInteract
) {

  fun getActiveSubscriptions(): Single<List<SubscriptionItem>> {
    return findDefaultWalletInteract.find()
        .flatMap { subscriptionApi.getActiveSubscriptions(it.address) }
        .map { subscriptions ->
          subscriptions.map { subscription ->
            SubscriptionItem(subscription.appName, subscription.packageName, subscription.iconUrl,
                subscription.amount, subscription.symbol, subscription.recurrence,
                subscription.expiresOn)
          }
        }
  }

  fun getExpiredSubscriptions(): Single<List<SubscriptionItem>> {
    return findDefaultWalletInteract.find()
        .flatMap { subscriptionApi.getExpiredSubscriptions(it.address) }
        .map { subscriptions ->
          subscriptions.map { subscription ->
            SubscriptionItem(subscription.appName, subscription.packageName, subscription.iconUrl,
                subscription.amount, subscription.symbol, subscription.recurrence,
                subscription.expiresOn)
          }
        }
  }

  fun getSubscriptionDetails(packageName: String): Single<SubscriptionDetails> {
    return findDefaultWalletInteract.find()
        .flatMap { wallet ->
          subscriptionApi.getSubscriptionDetails(packageName, wallet.address)
              .map { subscription -> mapSubscription(subscription) }
        }
        .onErrorReturn { EmptySubscriptionDetails() }
  }

  fun getSubscriptionByTrxId(transactionId: String): Single<SubscriptionDetails> {
    return subscriptionApi.getSubscriptionByTransactionId(transactionId)
        .map { mapSubscription(it) }
        .onErrorReturn { EmptySubscriptionDetails() }
  }

  private fun mapSubscription(subscription: Subscription): SubscriptionDetails {
    return if (subscription.active) {
      ActiveSubscriptionDetails(subscription.appName, subscription.packageName,
          subscription.iconUrl, subscription.amount, subscription.symbol, subscription.currency,
          subscription.recurrence, BigDecimal.ZERO, subscription.paymentMethod,
          subscription.paymentMethodIcon, subscription.nextPaymentDate, subscription.expiresOn)
    } else {
      ExpiredSubscriptionDetails(subscription.appName, subscription.packageName,
          subscription.iconUrl, subscription.amount, subscription.symbol, subscription.currency,
          subscription.recurrence, BigDecimal.ZERO, subscription.paymentMethod,
          subscription.paymentMethodIcon, subscription.lastBill!!, subscription.startDate)
    }
  }

}