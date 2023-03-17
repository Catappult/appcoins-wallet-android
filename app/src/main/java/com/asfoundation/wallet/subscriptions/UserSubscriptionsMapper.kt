package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.SubscriptionSubStatus
import com.appcoins.wallet.bdsbilling.subscriptions.UserSubscriptionsListResponse
import com.asfoundation.wallet.util.Period
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UserSubscriptionsMapper @Inject constructor() {

  internal companion object {
    internal const val DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
    internal val LOCALE = Locale.US
  }

  private fun mapSubscriptionList(
      subscriptionList: UserSubscriptionsListResponse): List<SubscriptionItem> {
    return subscriptionList.items.map {
      val application = it.application
      val order = it.order

      SubscriptionItem(it.sku, it.title, mapPeriod(it.period), mapStatus(it.subStatus),
          mapDate(it.started), mapDate(it.renewal), mapDate(it.expiry), mapDate(it.ended),
          application.name, application.title, application.icon, order.value, order.symbol,
          order.currency, order.method.title, order.method.logo, order.appc.value,
          order.appc.label, it.uid)
    }
  }

  fun mapToSubscriptionModel(all: UserSubscriptionsListResponse,
                             expired: UserSubscriptionsListResponse,
                             fromCache: Boolean = false): SubscriptionModel {
    val allSubsList = mapSubscriptionList(all)
    val expiredSubsList = mapSubscriptionList(expired)
    return SubscriptionModel(allSubsList, expiredSubsList, fromCache)
  }

  private fun mapStatus(subStatus: SubscriptionSubStatus): Status {
    return when (subStatus) {
      SubscriptionSubStatus.ACTIVE -> Status.ACTIVE
      SubscriptionSubStatus.CANCELED -> Status.CANCELED
      SubscriptionSubStatus.EXPIRED -> Status.EXPIRED
      SubscriptionSubStatus.PAUSED -> Status.PAUSED
      SubscriptionSubStatus.PENDING -> Status.PENDING
      SubscriptionSubStatus.REVOKED -> Status.REVOKED
      SubscriptionSubStatus.GRACE -> Status.GRACE
      SubscriptionSubStatus.HOLD -> Status.HOLD
    }
  }

  fun mapError(throwable: Throwable, fromCache: Boolean): SubscriptionModel {
    var error = SubscriptionModel.Error.UNKNOWN
    if (throwable.isNoNetworkException()) {
      error = SubscriptionModel.Error.NO_NETWORK
    }
    return SubscriptionModel(fromCache, error)
  }

  private fun mapPeriod(period: String): Period? {
    return Period.parse(period)
  }

  private fun mapDate(date: String?): Date? {
    return if (date == null) null
    else {
      val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US)
      dateFormat.parse(date)
    }
  }
}
