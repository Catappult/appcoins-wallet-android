package com.appcoins.wallet.core.analytics.analytics.legacy

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class PageViewAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

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