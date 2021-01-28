package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.WalletService
import io.reactivex.Single

class UserSubscriptionRepository(private val subscriptionApi: UserSubscriptionApi,
                                 private val accountWalletService: WalletService,
                                 private val subscriptionsMapper: UserSubscriptionsMapper) {

  fun getUserSubscriptions(subStatus: String? = null,
                           applicationName: String? = null,
                           limit: Int? = null): Single<UserSubscriptionListModel> {
    return accountWalletService.getAndSignCurrentWalletAddress()
        .flatMap {
          subscriptionApi.getUserSubscriptions(it.address, it.signedAddress, subStatus,
              applicationName, limit)
        }
        .map { subscriptionsMapper.mapSubscriptionList(it) }
        .onErrorReturn { subscriptionsMapper.mapError(it) }
  }
}