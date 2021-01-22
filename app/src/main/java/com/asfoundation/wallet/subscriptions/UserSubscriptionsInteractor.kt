package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.SubscriptionSubStatus
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class UserSubscriptionsInteractor(private val walletService: WalletService,
                                  private val remoteRepository: RemoteRepository,
                                  private val userSubscriptionRepository: UserSubscriptionRepository) {

  fun loadSubscriptions(): Single<SubscriptionModel> {
    return Single.zip(
        userSubscriptionRepository.getUserSubscriptions(),
        userSubscriptionRepository.getUserSubscriptions(SubscriptionSubStatus.EXPIRED.name,
            limit = 6),
        BiFunction { active, expired ->
          if (active.error != null && expired.error != null) {
            SubscriptionModel(true, active.error)
          } else if (active.userSubscriptionItems.isEmpty() && expired.userSubscriptionItems.isEmpty()) {
            SubscriptionModel(true, null)
          } else {
            SubscriptionModel(filterActive(active.userSubscriptionItems),
                expired.userSubscriptionItems)
          }
        }
    )
  }

  private fun filterActive(userSubscriptionItems: List<SubscriptionItem>): List<SubscriptionItem> {
    return userSubscriptionItems.filter { it.isActiveSubscription() }
  }

  fun cancelSubscription(packageName: String, uid: String): Single<Boolean> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          remoteRepository.cancelSubscription(packageName, uid, it.address, it.signedAddress)
        }
  }

  fun activateSubscription(packageName: String, uid: String): Single<Boolean> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          remoteRepository.activateSubscription(packageName, uid, it.address, it.signedAddress)
        }
  }
}
