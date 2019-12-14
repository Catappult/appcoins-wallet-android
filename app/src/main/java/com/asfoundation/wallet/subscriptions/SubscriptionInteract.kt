package com.asfoundation.wallet.subscriptions

import com.asfoundation.wallet.service.LocalCurrencyConversionService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class SubscriptionInteract(
    private val subscriptionRepository: SubscriptionRepository,
    private val localCurrencyConversionService: LocalCurrencyConversionService
) {

  fun loadSubscriptions(): Single<SubscriptionModel> {
    return Single.zip(
        subscriptionRepository.getActiveSubscriptions(),
        subscriptionRepository.getExpiredSubscriptions(),
        BiFunction { active, expired ->
          if (active.isEmpty() && expired.isEmpty()) {
            SubscriptionModel(emptyList(), emptyList(), true)
          } else {
            SubscriptionModel(active, expired)
          }
        }
    )
  }

  fun loadSubscriptionDetails(packageName: String): Single<SubscriptionDetails> {
    return subscriptionRepository.getSubscriptionDetails(packageName)
        .flatMap { details ->
          localCurrencyConversionService.getLocalToAppc(details.currency,
              details.amount.toString(), FIAT_SCALE)
              .singleOrError()
              .map {
                details.appcValue = it.amount
                details
              }
        }
  }

  fun cancelSubscription(packageName: String): Completable {
    return Completable.complete()
        .delay(1, TimeUnit.SECONDS)
  }

  companion object {
    private const val FIAT_SCALE = 2
  }
}
