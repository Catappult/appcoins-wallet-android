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
                subscription.amount, subscription.symbol)
          }
        }
  }

  fun getExpiredSubscriptions(): Single<List<SubscriptionItem>> {
    return findDefaultWalletInteract.find()
        .flatMap { subscriptionApiMocked.getExpiredSubscriptions(it.address) }
        .map { subscriptions ->
          subscriptions.map { subscription ->
            SubscriptionItem(subscription.appName, subscription.packageName, subscription.iconUrl,
                subscription.amount, subscription.symbol)
          }
        }
  }

  fun getSubscriptionDetails(packageName: String): Single<SubscriptionDetails> {
    return findDefaultWalletInteract.find()
        .flatMap { wallet ->
          subscriptionApiMocked.getSubscriptionDetails(packageName, wallet.address)
              .map { subscription ->
                SubscriptionDetails(
                    subscription.appName,
                    subscription.packageName,
                    subscription.iconUrl,
                    subscription.amount,
                    subscription.symbol,
                    subscription.currency,
                    SubscriptionStatus.ACTIVE.takeIf { subscription.active }
                        ?: SubscriptionStatus.EXPIRED,
                    BigDecimal.ZERO,
                    subscription.paymentMethod,
                    subscription.paymentMethodIcon,
                    subscription.nextPaymentDate,
                    subscription.lastBill,
                    subscription.startDate
                )
              }
        }
  }

  fun getSubscriptionByTrxId(transactionId: String): Single<SubscriptionDetails> {
    return subscriptionApiMocked.getSubscriptionByTransactionId(transactionId)
        .map { subscription ->
          SubscriptionDetails(
              subscription.appName,
              subscription.packageName,
              subscription.iconUrl,
              subscription.amount,
              subscription.symbol,
              subscription.currency,
              SubscriptionStatus.ACTIVE.takeIf { subscription.active }
                  ?: SubscriptionStatus.EXPIRED,
              BigDecimal.ZERO,
              subscription.paymentMethod,
              subscription.paymentMethodIcon,
              subscription.nextPaymentDate,
              subscription.lastBill,
              subscription.startDate
          )
        }

  }

}