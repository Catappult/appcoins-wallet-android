package com.appcoins.wallet.core.analytics.analytics.notification

import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.analytics.analytics.MatomoAnalytics.Companion.NOTIFICATION_TYPE
import com.appcoins.wallet.core.analytics.analytics.legacy.AnalyticsModule.Companion.WALLET_NOTIFICATION
import javax.inject.Inject

class NotificationAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager?) {
  fun sendNotificationReceivedAnalytics(notificationType: String) {
    val eventData = HashMap<String, Any>()
    eventData[NOTIFICATION_TYPE] = notificationType
    analyticsManager?.logEvent(
      eventData, WALLET_NOTIFICATION,
      AnalyticsManager.Action.IMPRESSION, WALLET
    )
  }

  fun sendNotificationClickAnalytics(notificationType: String) {
    val eventData = HashMap<String, Any>()
    eventData[NOTIFICATION_TYPE] = notificationType
    analyticsManager?.logEvent(
      eventData, WALLET_NOTIFICATION,
      AnalyticsManager.Action.CLICK, WALLET
    )
  }

  private companion object {
    const val WALLET = "wallet_notification"
  }
}