package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.core.network.microservices.model.*
import com.asfoundation.wallet.subscriptions.db.UserSubscriptionEntity
import com.asfoundation.wallet.subscriptions.db.UserSubscriptionsDao
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class UserSubscriptionsLocalData @Inject constructor(
    private val userSubscriptionsDao: UserSubscriptionsDao) {

  fun insertSubscriptions(responseList: List<UserSubscriptionResponse>,
                          walletAddress: String) {
    return userSubscriptionsDao.insertSubscriptions(mapToEntity(responseList, walletAddress))
  }

  fun getSubscriptions(walletAddress: String, subscriptionSubStatus: SubscriptionSubStatus? = null,
                       limit: Int? = null): Observable<UserSubscriptionsListResponse> {
    return buildGetSubscriptionQuery(walletAddress, subscriptionSubStatus, limit)
        .map { mapToResponse(it) }
        .toObservable()
  }

  private fun buildGetSubscriptionQuery(walletAddress: String,
                                        subscriptionSubStatus: SubscriptionSubStatus?,
                                        limit: Int?): Single<List<UserSubscriptionEntity>> {
    return when {
      subscriptionSubStatus != null && limit != null -> {
        userSubscriptionsDao.getSubscriptionsBySubStatusWithLimit(walletAddress,
            subscriptionSubStatus, limit)
      }
      subscriptionSubStatus != null && limit == null -> {
        userSubscriptionsDao.getSubscriptionsByStatus(walletAddress, subscriptionSubStatus)
      }
      subscriptionSubStatus == null && limit != null -> {
        userSubscriptionsDao.getSubscriptionsWithLimit(walletAddress, limit)
      }
      else -> userSubscriptionsDao.getSubscriptions(walletAddress)
    }
  }

  private fun mapToEntity(
    responseList: List<UserSubscriptionResponse>,
    walletAddress: String
  ): List<UserSubscriptionEntity> {
    val entityList: MutableList<UserSubscriptionEntity> = ArrayList()
    for (response in responseList) {
      val application = response.application
      val order = response.order
      val method = order.method
      val appc = order.appc
      val entity = UserSubscriptionEntity(
        response.uid, walletAddress, response.sku, response.title,
        response.period, response.subStatus, response.started, response.renewal, response.expiry,
        response.ended, application.name, application.title, application.icon, order.gateway,
        order.reference, order.value, order.label, order.currency, order.symbol, order.created,
          method.name, method.title, method.logo, appc.value, appc.label)
      entityList.add(entity)
    }
    return entityList
  }

  private fun mapToResponse(
      entityList: List<UserSubscriptionEntity>): UserSubscriptionsListResponse {
    val responseList: MutableList<UserSubscriptionResponse> = ArrayList()
    for (entity in entityList) {
      val response = UserSubscriptionResponse(entity.uid, entity.sku, entity.title, entity.period,
          entity.subStatus, entity.started, entity.renewal, entity.expire, entity.ended,
          ApplicationInfoResponse(entity.appName, entity.appTitle, entity.appIcon),
          OrderResponse(entity.gateway, entity.reference, entity.value, entity.label,
              entity.currency, entity.symbol, entity.created,
              MethodResponse(entity.methodName, entity.methodTitle, entity.methodLogo),
              AppcPrice(entity.appcValue, entity.appcLabel)))
      responseList.add(response)
    }
    return UserSubscriptionsListResponse(responseList)
  }
}
