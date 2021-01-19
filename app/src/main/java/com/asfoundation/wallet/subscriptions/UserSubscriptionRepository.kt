package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.SubscriptionSubStatus
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import io.reactivex.Single

class UserSubscriptionRepository(private val subscriptionApi: SubscriptionBillingApi,
                                 private val accountWalletService: WalletService,
                                 private val subscriptionsMapper: UserSubscriptionsMapper) {

  fun getUserSubscriptions(subStatus: List<String>? = null,
                           applicationName: List<String>? = null,
                           limit: Int? = null): Single<UserSubscriptionListModel> {
    return accountWalletService.getAndSignCurrentWalletAddress()
        .flatMap {
          subscriptionApi.getUserSubscriptions(it.address, it.signedAddress, subStatus,
              applicationName, limit)
        }
        .map { subscriptionsMapper.mapSubscriptionList(it) }
        .onErrorReturn { subscriptionsMapper.mapError(it) }
  }

  fun getActiveSubStatus(): List<String> {
    return listOf(SubscriptionSubStatus.ACTIVE.name, SubscriptionSubStatus.CANCELED.name,
        SubscriptionSubStatus.PAUSED.name)
  }

  fun getExpiredSubStatus(): List<String> {
    return listOf(SubscriptionSubStatus.EXPIRED.name)
  }
}