package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import io.reactivex.Completable
import io.reactivex.Observable

class UserSubscriptionsInteractor(private val walletService: WalletService,
                                  private val remoteRepository: RemoteRepository,
                                  private val userSubscriptionRepository: UserSubscriptionRepository) {

  fun loadSubscriptions(freshReload: Boolean): Observable<SubscriptionModel> {
    return walletService.getWalletAddress()
        .flatMapObservable { userSubscriptionRepository.getUserSubscriptions(it, freshReload) }
  }

  fun cancelSubscription(packageName: String, uid: String): Completable {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMapCompletable {
          remoteRepository.cancelSubscription(packageName, uid, it.address, it.signedAddress)
        }
  }

  fun activateSubscription(packageName: String, uid: String): Completable {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMapCompletable {
          remoteRepository.activateSubscription(packageName, uid, it.address, it.signedAddress)
        }
  }
}
