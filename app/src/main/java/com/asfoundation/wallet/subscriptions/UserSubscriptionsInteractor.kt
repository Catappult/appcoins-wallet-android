package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class UserSubscriptionsInteractor @Inject constructor(
  private val walletService: WalletService,
  private val remoteRepository: RemoteRepository,
  private val userSubscriptionRepository: UserSubscriptionRepository
) {

  fun loadSubscriptions(freshReload: Boolean): Observable<SubscriptionModel> {
    return walletService.getWalletAddress()
      .flatMapObservable { userSubscriptionRepository.getUserSubscriptions(it, freshReload) }
  }

  fun cancelSubscription(packageName: String, uid: String): Completable {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMapCompletable {
        remoteRepository.cancelSubscription(
          packageName = packageName,
          uid = uid,
          walletAddress = it.address
        )
      }
  }

  fun activateSubscription(packageName: String, uid: String): Completable {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMapCompletable {
        remoteRepository.activateSubscription(
          packageName = packageName,
          uid = uid,
          walletAddress = it.address
        )
      }
  }
}
