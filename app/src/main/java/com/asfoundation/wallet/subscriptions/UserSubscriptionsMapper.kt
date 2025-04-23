package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.core.network.microservices.model.SubscriptionSubStatus
import com.appcoins.wallet.core.network.microservices.model.UserSubscriptionsListResponse
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.asfoundation.wallet.util.Period
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class UserSubscriptionsMapper @Inject constructor() {

  internal companion object {
    internal const val DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
    internal val LOCALE = Locale.US
  }

  private fun mapSubscriptionList(
    subscriptionList: UserSubscriptionsListResponse
  ): List<SubscriptionItem> {
    return subscriptionList.items.map {
      val application = it.application
      val order = it.order

      SubscriptionItem(
        sku = it.sku,
        itemName = it.title,
        period = mapPeriod(it.period),
        status = mapStatus(it.subStatus),
        started = mapDate(it.started),
        renewal = mapDate(it.renewal),
        expiry = mapDate(it.expiry),
        ended = mapDate(it.ended),
        packageName = application.name,
        appName = application.title ?: "",
        appIcon = application.icon ?: "",
        fiatAmount = order.value,
        fiatSymbol = order.symbol,
        currency = order.currency,
        paymentMethod = order.method.title,
        paymentIcon = order.method.logo,
        appcAmount = order.appc.value,
        appcLabel = order.appc.label,
        uid = it.uid,
        isFreeTrial = it.trialing,
      )
    }
  }

  fun mapToSubscriptionModel(
    all: UserSubscriptionsListResponse,
    expired: UserSubscriptionsListResponse,
    fromCache: Boolean = false
  ): SubscriptionModel {
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
