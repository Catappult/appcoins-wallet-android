package com.asfoundation.wallet.main.nav_bar

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class NavBarAnalytics @Inject constructor(private val analytics: AnalyticsManager) {

  companion object {
    const val WALLET_CALLOUT_PROMOTIONS_CLICK = "wallet_callout_promotions_click"
    private const val WALLET = "WALLET"
  }

  fun sendCallOutEvent() {
    val data = HashMap<String, Any>()
    analytics.logEvent(data, WALLET_CALLOUT_PROMOTIONS_CLICK, AnalyticsManager.Action.CLICK, WALLET)
  }
}