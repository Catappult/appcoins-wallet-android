package com.appcoins.wallet.core.analytics.analytics

import android.util.Log
import cm.aptoide.analytics.AnalyticsManager
import cm.aptoide.analytics.EventLogger
import com.indicative.client.android.Indicative
import javax.inject.Inject

class IndicativeEventLogger @Inject constructor(
  private val indicativeAnalytics: IndicativeAnalytics
) : EventLogger {

  companion object {
    private const val TAG = "IndicativeEventLogger"
  }

  override fun setup() = Unit

  override fun log(
    eventName: String, data: Map<String, Any>?,
    action: AnalyticsManager.Action, context: String
  ) {

    // Concats the data and superProperties. This way we can mimic Rakam's superProperties.
    val superPropertiesAndData: Map<String, Any>?
    superPropertiesAndData = indicativeAnalytics.superProperties + (data ?: HashMap())
    Indicative.recordEvent(eventName, indicativeAnalytics.usrId, superPropertiesAndData)

    Log.d(
      TAG,
      "log() called with: eventName = [$eventName], superProperties = [${indicativeAnalytics.superProperties}] data = [$data], action = [$action], context = [$context], userId = [${indicativeAnalytics.usrId}]"
    )
  }

}