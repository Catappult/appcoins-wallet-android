package com.appcoins.wallet.core.analytics.analytics

import cm.aptoide.analytics.AnalyticsManager
import cm.aptoide.analytics.EventLogger
import io.sentry.Sentry
import io.sentry.SentryLevel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class SentryEventLogger @Inject constructor() : EventLogger {

  val enabled: AtomicBoolean = AtomicBoolean(true)

  override fun setup() = Unit

  override fun log(
    eventName: String,
    data: Map<String, Any>?,
    action: AnalyticsManager.Action,
    context: String
  ) {
    if (enabled.get()) {
      val level = if (action in listOf(
          AnalyticsManager.Action.AUTO,
          AnalyticsManager.Action.ROOT,
          AnalyticsManager.Action.VIEW,
          AnalyticsManager.Action.INSTALL,
          AnalyticsManager.Action.IMPRESSION
        )) SentryLevel.INFO else SentryLevel.DEBUG

      Sentry.captureMessage(eventName) { scope ->
        scope.level = level
        scope.setTag("category", action.toString())
        data?.mapValues { it.value.toString() }?.forEach{ (key, value) ->
          scope.setExtra(key, value)
        }
      }
    }
  }
}