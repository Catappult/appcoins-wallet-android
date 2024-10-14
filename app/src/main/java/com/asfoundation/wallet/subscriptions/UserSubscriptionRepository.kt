package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.model.SubscriptionSubStatus
import com.appcoins.wallet.core.network.microservices.model.SubscriptionSubStatus.EXPIRED
import com.appcoins.wallet.core.network.microservices.model.UserSubscriptionApi
import com.appcoins.wallet.core.network.microservices.model.UserSubscriptionsListResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.Locale
import javax.inject.Inject

class UserSubscriptionRepository @Inject constructor(
  private val subscriptionApi: UserSubscriptionApi,
  private val local: UserSubscriptionsLocalData,
  private val walletService: WalletService,
  private val subscriptionsMapper: UserSubscriptionsMapper,
  private val ewtObtainer: EwtAuthenticatorService,
  private val rxSchedulers: RxSchedulers,
) {

  fun getUserSubscriptions(
    walletAddress: String,
    freshReload: Boolean
  ): Observable<SubscriptionModel> {
    val apiCall = getApiUserSubscriptions(walletAddress)
    return if (freshReload) apiCall
    else {
      val dbCall = getDbUserSubscriptions(walletAddress)
      Observable.concat(dbCall, apiCall)
    }

  }

  private fun getDbUserSubscriptions(walletAddress: String): Observable<SubscriptionModel> {
    return Observable.zip(local.getSubscriptions(walletAddress),
      local.getSubscriptions(walletAddress, EXPIRED, EXPIRED_SUBS_LIMIT),
      BiFunction { active: UserSubscriptionsListResponse, expired: UserSubscriptionsListResponse ->
        subscriptionsMapper.mapToSubscriptionModel(active, expired, true)
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
          getUserSubscriptionAndSave(
            languageTag, walletAddress, it, EXPIRED,
            EXPIRED_SUBS_LIMIT
          ),
          BiFunction { active: UserSubscriptionsListResponse, expired: UserSubscriptionsListResponse ->
            subscriptionsMapper.mapToSubscriptionModel(active, expired)
          })
      }
      .onErrorReturn {
        it.printStackTrace()
        subscriptionsMapper.mapError(it, false)
      }
  }

  private fun getUserSubscriptionAndSave(
    languageTag: String, walletAddress: String,
    signature: String, status: SubscriptionSubStatus? = null,
    limit: Int? = null
  ): Observable<UserSubscriptionsListResponse> {
    return subscriptionApi.getUserSubscriptions(
      language = languageTag,
      walletAddress = walletAddress,
      walletSignature = signature,
      subStatus = status?.name,
      limit = limit
    )
      .doOnSuccess { local.insertSubscriptions(it.items, walletAddress) }
      .toObservable()
  }

  private companion object {
    private const val EXPIRED_SUBS_LIMIT = 6
  }
}
