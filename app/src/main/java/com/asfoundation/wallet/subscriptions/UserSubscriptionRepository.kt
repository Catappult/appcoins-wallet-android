package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.SubscriptionSubStatus
import com.appcoins.wallet.bdsbilling.SubscriptionSubStatus.EXPIRED
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.subscriptions.UserSubscriptionsListResponse
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.*

class UserSubscriptionRepository(private val subscriptionApi: UserSubscriptionApi,
                                 private val local: UserSubscriptionsLocalData,
                                 private val walletService: WalletService,
                                 private val subscriptionsMapper: UserSubscriptionsMapper) {

  fun getUserSubscriptions(walletAddress: String): Observable<SubscriptionModel> {
    val dbCall = getDbUserSubscriptions(walletAddress)
    val apiCall = getApiUserSubscriptions(walletAddress)
    return Observable.concat(dbCall, apiCall)
  }

  private fun getDbUserSubscriptions(walletAddress: String): Observable<SubscriptionModel> {
    return Observable.zip(local.getSubscriptions(walletAddress),
        local.getSubscriptions(walletAddress, EXPIRED, 6),
        BiFunction { active: UserSubscriptionsListResponse, expired: UserSubscriptionsListResponse ->
          val activeModel = subscriptionsMapper.mapSubscriptionList(active)
          val expiredModel = subscriptionsMapper.mapSubscriptionList(expired)
          subscriptionsMapper.mapToSubscriptionModel(activeModel, expiredModel, true)
        })
        .onErrorReturn {
          it.printStackTrace()
          subscriptionsMapper.mapError(it, true)
        }
  }

  private fun getApiUserSubscriptions(walletAddress: String): Observable<SubscriptionModel> {
    return walletService.signContent(walletAddress)
        .flatMapObservable {
          val languageTag = Locale.getDefault()
              .toLanguageTag()
          Observable.zip(getUserSubscriptionAndSave(languageTag, walletAddress, it),
              getUserSubscriptionAndSave(languageTag, walletAddress, it, EXPIRED, 6),
              BiFunction { active: UserSubscriptionsListResponse, expired: UserSubscriptionsListResponse ->
                val activeModel = subscriptionsMapper.mapSubscriptionList(active)
                val expiredModel = subscriptionsMapper.mapSubscriptionList(expired)
                subscriptionsMapper.mapToSubscriptionModel(activeModel, expiredModel)
              })
        }
        .onErrorReturn {
          it.printStackTrace()
          subscriptionsMapper.mapError(it, false)
        }
  }

  private fun getUserSubscriptionAndSave(languageTag: String, walletAddress: String,
                                         signature: String, status: SubscriptionSubStatus? = null,
                                         limit: Int? = null): Observable<UserSubscriptionsListResponse> {
    return subscriptionApi.getUserSubscriptions(languageTag, walletAddress, signature, status?.name,
        limit)
        .doOnSuccess { local.insertSubscriptions(it.items, walletAddress) }
        .toObservable()
  }
}
