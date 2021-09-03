package com.asfoundation.wallet.billing.analytics

import cm.aptoide.analytics.AnalyticsManager
import java.util.*

class PageViewAnalytics(private val analyticsManager: AnalyticsManager) {

  fun sendPageViewEvent(context: String) {
    val eventData = HashMap<String, Any>()

    eventData[CONTEXT] = context

    analyticsManager.logEvent(eventData, WALLET_PAGE_VIEW,
        AnalyticsManager.Action.CLICK, WALLET)
  }

  companion object {
    const val WALLET_PAGE_VIEW = "wallet_page_view"

    private const val CONTEXT = "context"

    private const val WALLET = "wallet"
  }
}