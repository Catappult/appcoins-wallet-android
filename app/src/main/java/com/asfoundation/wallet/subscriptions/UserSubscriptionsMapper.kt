package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.SubscriptionSubStatus
import com.appcoins.wallet.bdsbilling.subscriptions.UserSubscriptionsListResponse
import com.asfoundation.wallet.util.Period
import com.asfoundation.wallet.util.isNoNetworkException
import java.text.SimpleDateFormat
import java.util.*

class UserSubscriptionsMapper {

  private fun mapSubscriptionList(
      subscriptionList: UserSubscriptionsListResponse): UserSubscriptionListModel {
    return UserSubscriptionListModel(
        subscriptionList.items.map {
          val application = it.application
          val order = it.order
          SubscriptionItem(it.title, mapPeriod(it.period), mapStatus(it.subStatus),
              mapDate(it.started), mapDate(it.renewal), mapDate(it.expire), mapDate(it.ended),
              application.name, application.title, application.icon, order.value, order.symbol,
              order.currency, order.method.title, order.method.logo, order.appc.value,
              order.appc.label, it.uid)
        })
  }

  fun mapToSubscriptionModel(all: UserSubscriptionsListResponse,
                             expired: UserSubscriptionsListResponse,
                             fromCache: Boolean = false): SubscriptionModel {
    val allSubsModel = mapSubscriptionList(all)
    val expiredSubsModel = mapSubscriptionList(expired)
    return if (allSubsModel.error != null && expiredSubsModel.error != null) {
      SubscriptionModel(fromCache, allSubsModel.error)
    } else {
      SubscriptionModel(allSubsModel.userSubscriptionItems,
          expiredSubsModel.userSubscriptionItems, fromCache = fromCache)
    }
  }

  private fun mapStatus(subStatus: SubscriptionSubStatus): Status {
    return when (subStatus) {
      SubscriptionSubStatus.ACTIVE -> Status.ACTIVE
      SubscriptionSubStatus.CANCELED -> Status.CANCELED
      SubscriptionSubStatus.EXPIRED -> Status.EXPIRED
      SubscriptionSubStatus.PAUSED -> Status.PAUSED
      SubscriptionSubStatus.PENDING -> Status.PENDING
      SubscriptionSubStatus.REVOKED -> Status.REVOKED
    }
  }

  fun mapError(throwable: Throwable, fromCache: Boolean): SubscriptionModel {
    var error = Error.UNKNOWN
    if (throwable.isNoNetworkException()) {
      error = Error.NO_NETWORK
    }
    return SubscriptionModel(fromCache, error)
  }

  private fun mapPeriod(period: String): Period? {
    return Period.parse(period)
  }

  private fun mapDate(date: String?): Date? {
    return if (date == null) null
    else {
      val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
      dateFormat.parse(date)
    }
  }
}
