package com.appcoins.wallet.core.analytics.analytics

import cm.aptoide.analytics.AnalyticsManager
import cm.aptoide.analytics.EventLogger
import io.sentry.Sentry
import io.sentry.event.Breadcrumb
import io.sentry.event.BreadcrumbBuilder
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
            Sentry.getContext().recordBreadcrumb(
                BreadcrumbBuilder()
                    .setType(
                        if (action in listOf(
                                AnalyticsManager.Action.AUTO,
                                AnalyticsManager.Action.ROOT,
                                AnalyticsManager.Action.VIEW,
                                AnalyticsManager.Action.INSTALL,
                                AnalyticsManager.Action.IMPRESSION
                            )
                        ) Breadcrumb.Type.DEFAULT else Breadcrumb.Type.USER
                    )
                    .setLevel(Breadcrumb.Level.INFO)
                    .setMessage(eventName)
                    .setCategory(action.toString())
                    .apply {
                        setData(data?.mapValuesTo(mutableMapOf()) { it.toString() }
                            ?: mutableMapOf())
                    }
                    .build()
            )
    }
}