package com.asfoundation.wallet.analytics

import android.util.Log
import cm.aptoide.analytics.AnalyticsManager
import cm.aptoide.analytics.EventLogger
import com.amplitude.api.Amplitude
import com.asf.wallet.BuildConfig
import org.json.JSONException
import org.json.JSONObject

class AmplitudeEventLogger : EventLogger {

  companion object {
    const val APTOIDE_PACKAGE = "aptoide_package"
    const val VERSION_CODE = "version_code"
    const val ENTRY_POINT = "entry_point"
    const val USER_LEVEL = "user_level"
    const val HAS_GMS = "has_gms"
    private const val TAG = "AmplitudeEventLogger"
  }

  override fun setup() = Unit

  override fun log(eventName: String?, data: MutableMap<String, Any>?,
                   action: AnalyticsManager.Action?, context: String?) {
    if (data != null) {
      Amplitude.getInstance()
          .logEvent(eventName, mapToJsonObject(data))
    } else {
      Amplitude.getInstance()
          .logEvent(eventName)
    }

    if (BuildConfig.LOGGABLE) {
      Log.d(TAG,
          "log() called with: eventName = [$eventName], data = [$data], action = [$action], context = [$context]")
    }
  }

  private fun mapToJsonObject(data: Map<String, Any>): JSONObject {
    val eventData = JSONObject()

    for (entry in data.entries) {
      try {
        eventData.put(entry.key, entry.value.toString())
      } catch (e: JSONException) {
        e.printStackTrace()
      }
    }
    return eventData
  }
}