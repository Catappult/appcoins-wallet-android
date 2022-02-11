package com.asfoundation.wallet.analytics

import cm.aptoide.analytics.AnalyticsManager
import cm.aptoide.analytics.EventLogger
import com.asfoundation.wallet.util.Log
import com.indicative.client.android.Indicative

class IndicativeEventLogger(val indicativeAnalytics: IndicativeAnalytics) : EventLogger {

  companion object {
    private const val TAG = "IndicativeEventLogger"
  }

  override fun setup() = Unit

  override fun log(eventName: String, data: Map<String, Any>?,
                   action: AnalyticsManager.Action, context: String) {

    // Concats the data and superProperties. This way we can mimic Rakam's superProperties.
    var superPropertiesAndData: Map<String, Any>? = HashMap()
    superPropertiesAndData = indicativeAnalytics.superProperties + (data ?: HashMap())

    if (superPropertiesAndData != null) {
      Indicative.recordEvent(eventName, indicativeAnalytics.usrId, superPropertiesAndData)
    } else {
      Indicative.recordEvent(eventName, indicativeAnalytics.usrId, HashMap<String, Any>())
    }

    Log.d(TAG,
        "log() called with: eventName = [$eventName], superProperties = [${indicativeAnalytics.superProperties}] data = [$data], action = [$action], context = [$context], userId = [${indicativeAnalytics.usrId}]")
  }

}