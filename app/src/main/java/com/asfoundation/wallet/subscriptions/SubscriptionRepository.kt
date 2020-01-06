package com.asfoundation.wallet.subscriptions

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Single
import java.math.BigDecimal

class SubscriptionRepository(
    private val subscriptionApi: SubscriptionApi,//TODO unused until microservices are ready
    private val subscriptionApiMocked: SubscriptionApiMocked,
    private val findDefaultWalletInteract: FindDefaultWalletInteract
) {

  fun getActiveSubscriptions(): Single<List<SubscriptionItem>> {
    return findDefaultWalletInteract.find()
        .flatMap { subscriptionApiMocked.getActiveSubscriptions(it.address) }
        .map { subscriptions ->
          subscriptions.map { subscription ->
            SubscriptionItem(subscription.appName, subscription.packageName, subscription.iconUrl,
                subscription.amount, subscription.symbol, subscription.recurrence)
          }
        }
  }

  fun getExpiredSubscriptions(): Single<List<SubscriptionItem>> {
    return findDefaultWalletInteract.find()
        .flatMap { subscriptionApiMocked.getExpiredSubscriptions(it.address) }
        .map { subscriptions ->
          subscriptions.map { subscription ->
            SubscriptionItem(subscription.appName, subscription.packageName, subscription.iconUrl,
                subscription.amount, subscription.symbol, subscription.recurrence)
          }
        }
  }

  fun getSubscriptionDetails(packageName: String): Single<SubscriptionDetails> {
    return findDefaultWalletInteract.find()
        .flatMap { wallet ->
          subscriptionApiMocked.getSubscriptionDetails(packageName, wallet.address)
              .map { subscription -> mapSubscription(subscription) }
        }
        .onErrorReturn { EmptySubscriptionDetails() }
  }

  fun getSubscriptionByTrxId(transactionId: String): Single<SubscriptionDetails> {
    return subscriptionApiMocked.getSubscriptionByTransactionId(transactionId)
        .map { mapSubscription(it) }
        .onErrorReturn { EmptySubscriptionDetails() }
  }

  private fun mapSubscription(subscription: Subscription): SubscriptionDetails {
    return if (subscription.active) {
      ActiveSubscriptionDetails(subscription.appName, subscription.packageName,
          subscription.iconUrl, subscription.amount, subscription.symbol, subscription.currency,
          subscription.recurrence, BigDecimal.ZERO, subscription.paymentMethod,
          subscription.paymentMethodIcon, subscription.nextPaymentDate!!)
    } else {
      ExpiredSubscriptionDetails(subscription.appName, subscription.packageName,
          subscription.iconUrl, subscription.amount, subscription.symbol, subscription.currency,
          subscription.recurrence, BigDecimal.ZERO, subscription.paymentMethod,
          subscription.paymentMethodIcon, subscription.lastBill!!, subscription.startDate)
    }
  }

}