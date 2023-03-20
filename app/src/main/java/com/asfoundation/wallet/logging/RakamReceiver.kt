package com.asfoundation.wallet.logging

import com.appcoins.wallet.core.utils.jvm_common.LogReceiver
import com.appcoins.wallet.core.utils.jvm_common.LogReceiver.Companion.DEFAULT_MSG
import com.appcoins.wallet.core.utils.jvm_common.LogReceiver.Companion.DEFAULT_TAG
import com.appcoins.wallet.core.utils.jvm_common.LogReceiver.Companion.DEFAULT_THROWABLE_STATCKTRACE
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

  override fun log(tag: String?, message: String?, asError: Boolean, addToBreadcrumbs: Boolean) {
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
        properties.put("throwable_stacktrace", getStacktrace(it.stackTrace))
      }
    } catch (e: JSONException) {
      e.printStackTrace()
    }
    return properties
  }

  private fun getStacktrace(stackTrace: Array<StackTraceElement>?): String {
    return if (stackTrace != null) {
      //This is done because rakam has a limit of characters and if we pass the entire stacktrace
      // the critical information doesn't show up
      buildStackTraceString(stackTrace)
    } else {
      DEFAULT_THROWABLE_STATCKTRACE
    }
  }

  private fun buildStackTraceString(stackTrace: Array<StackTraceElement>): String {
    var firstTraceString = ""
    var secondTraceString = ""
    if (stackTrace.isNotEmpty()) {
      firstTraceString =
          "M:${stackTrace[0].methodName},L:${stackTrace[0].lineNumber}"
    }
    if (stackTrace.size > 1) {
      secondTraceString =
          "C:${stackTrace[1].className},M:${stackTrace[1].methodName},L:${stackTrace[1].lineNumber}"
    }
    return "$firstTraceString / $secondTraceString"
  }
}