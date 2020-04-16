package com.asfoundation.wallet.logging

import com.asfoundation.wallet.logging.LogReceiver.Companion.DEFAULT_MSG
import com.asfoundation.wallet.logging.LogReceiver.Companion.DEFAULT_TAG
import com.asfoundation.wallet.logging.LogReceiver.Companion.DEFAULT_THROWABLE_STATCKTRACE
import io.rakam.api.Rakam
import org.json.JSONException
import org.json.JSONObject

class RakamReceiver : LogReceiver {
  companion object {
    private const val LOG_EVENT_TYPE = "wallet_non_fatal_event"
  }

  override fun log(tag: String?, throwable: Throwable?) {
    Rakam.getInstance()
        .logEvent(LOG_EVENT_TYPE, map(tag, null, throwable))
  }

  override fun log(tag: String?, message: String?) {
    Rakam.getInstance()
        .logEvent(LOG_EVENT_TYPE, map(tag, message, null))
  }

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    Rakam.getInstance()
        .logEvent(LOG_EVENT_TYPE, map(tag, message, throwable))
  }

  private fun map(tag: String?, message: String?, throwable: Throwable?): JSONObject {
    val properties = JSONObject()
    try {
      properties.put("tag", tag ?: DEFAULT_TAG)
      message?.let {
        properties.put("message", message)
      }
      throwable?.let {
        properties.put("throwable_message", it.message ?: DEFAULT_MSG)
        properties.put("throwable_stacktrace",
            if (it.stackTrace != null) it.stackTrace!!.contentToString() else DEFAULT_THROWABLE_STATCKTRACE)
      }
    } catch (e: JSONException) {
      e.printStackTrace()
    }
    return properties
  }
}