package com.appcoins.wallet.core.analytics.analytics

import cm.aptoide.analytics.AnalyticsManager
import cm.aptoide.analytics.EventLogger
import com.appcoins.wallet.sharedpreferences.AppStartPreferencesDataSource
import org.json.JSONObject
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import javax.inject.Inject

class MatomoAnalytics @Inject constructor(
  private val tracker: Tracker,
  private val indicativeAnalytics: IndicativeAnalytics,
) : EventLogger {

  companion object {
    const val NOTIFICATION_TYPE = "notification_type"
  }

  override fun setup() = Unit

  override fun log(
    eventName: String,
    data: Map<String, Any>?,
    action: AnalyticsManager.Action,
    context: String
  ) {
    if (indicativeAnalytics.usrId.isNotEmpty())
      tracker.userId = indicativeAnalytics.usrId

    val notificationType = data?.get(NOTIFICATION_TYPE)?.toString()

    val completedData: Map<String, Any> = (data ?: emptyMap()) - NOTIFICATION_TYPE + mapOf(
      AnalyticsLabels.DEVICE_ORIENTATION to indicativeAnalytics.findDeviceOrientation(),
    )

    val json = JSONObject(completedData.mapValues { it.value.toString() }).toString()

    TrackHelper.track()
      .apply { notificationType?.let { dimension(CustomDimension.NOTIFICATION_TYPE.id, it) } }
      .dimension(CustomDimension.ACTION.id, json)
      .event(context, action.mapAction(isNotification = notificationType != null))
      .name(eventName)
      .with(tracker)
    tracker.dispatch()
  }
}

private const val RECEIVED_NOTIFICATION = "received_notification"
private const val CLICKED_NOTIFICATION = "clicked_notification"

private enum class CustomDimension(val id: Int) {
  NOTIFICATION_TYPE(8),
  ACTION(4)
}

private fun AnalyticsManager.Action.mapAction(isNotification: Boolean): String =
  when {
    this == AnalyticsManager.Action.IMPRESSION && isNotification -> return RECEIVED_NOTIFICATION
    this == AnalyticsManager.Action.CLICK && isNotification -> return CLICKED_NOTIFICATION
    else -> this.name
  }