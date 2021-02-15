package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import io.reactivex.Observable
import io.reactivex.Single

class UserSubscriptionsInteractor(private val walletService: WalletService,
                                  private val remoteRepository: RemoteRepository,
                                  private val userSubscriptionRepository: UserSubscriptionRepository) {

  fun loadSubscriptions(): Observable<SubscriptionModel> {
    return walletService.getWalletAddress()
        .flatMapObservable { userSubscriptionRepository.getUserSubscriptions(it) }
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
