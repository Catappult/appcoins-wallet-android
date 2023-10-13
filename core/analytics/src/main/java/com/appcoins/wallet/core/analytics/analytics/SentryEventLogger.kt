package com.appcoins.wallet.core.analytics.analytics

import cm.aptoide.analytics.AnalyticsManager
import cm.aptoide.analytics.EventLogger
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryItemType
import io.sentry.SentryLevel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class SentryEventLogger @Inject constructor() : EventLogger {

    val enabled: AtomicBoolean = AtomicBoolean(true)

    override fun setup() = Unit

    override fun log(
        eventName: String, data: Map<String, Any>?,
        action: AnalyticsManager.Action, context: String
    ) {
      if (enabled.get())
        Sentry.addBreadcrumb(
          Breadcrumb(eventName).apply {
            type = if (action in listOf(
                AnalyticsManager.Action.AUTO,
                AnalyticsManager.Action.ROOT,
                AnalyticsManager.Action.VIEW,
                AnalyticsManager.Action.INSTALL,
                AnalyticsManager.Action.IMPRESSION
              )
            ) SentryItemType.Event.itemType else SentryItemType.UserFeedback.itemType
            level = SentryLevel.INFO
            category = action.toString()
          }
        )
    }
}