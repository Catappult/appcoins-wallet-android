package com.asfoundation.wallet.subscriptions

import com.appcoins.wallet.bdsbilling.SubscriptionSubStatus
import com.appcoins.wallet.bdsbilling.subscriptions.UserSubscriptionsListResponse
import com.asfoundation.wallet.util.Period
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class UserSubscriptionsMapper {

  fun mapSubscriptionList(
      subscriptionList: UserSubscriptionsListResponse): UserSubscriptionListModel {
    return UserSubscriptionListModel(
        subscriptionList.items.map {
          val application = it.application
          val order = it.order
          SubscriptionItem(it.title, mapPeriod(it.period), mapStatus(it.subStatus),
              mapDate(it.started), mapDate(it.renewal), mapDate(it.expire), mapDate(it.ended),
              application.name, application.title, application.icon, order.value, order.symbol,
              order.currency, order.method.title, order.method.icon, order.appc.value,
              order.appc.label, it.uid)
        })
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

  fun mapError(throwable: Throwable): UserSubscriptionListModel {
    return UserSubscriptionListModel(listOf(
        SubscriptionItem(
            "name", mapPeriod("P1M"), Status.ACTIVE,
            mapDate("2021-01-05T21:33:35.997618Z"), mapDate("2021-01-05T21:33:35.997618Z"), null,
            null, "nam2e", "title",
            "https://cdn6.aptoide.com/imgs/0/2/b/02b57118b06b81958ab1baf4788ce09d_logo.png",
            BigDecimal(1), "€", "EUR", "CC",
            "https://cdn6.aptoide.com/imgs/0/2/b/02b57118b06b81958ab1baf4788ce09d_logo.png",
            BigDecimal(2), "APPC", "uid"), SubscriptionItem(
        "name2", mapPeriod("P1M"), Status.EXPIRED,
        null, null, mapDate("2021-01-05T21:33:35.997618Z"), mapDate("2021-01-05T21:33:35.997618Z"),
        "nam2e2", "title",
        "https://cdn6.aptoide.com/imgs/0/2/b/02b57118b06b81958ab1baf4788ce09d_logo.png",
        BigDecimal(1), "€", "EUR", "CC",
        "https://cdn6.aptoide.com/imgs/0/2/b/02b57118b06b81958ab1baf4788ce09d_logo.png",
        BigDecimal(2), "APPC", "uid")))
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
