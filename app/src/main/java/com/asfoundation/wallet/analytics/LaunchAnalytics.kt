package com.asfoundation.wallet.analytics

import cm.aptoide.analytics.AnalyticsManager
import java.util.*
import javax.inject.Inject

class LaunchAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  companion object {
    private const val WALLET = "WALLET"
    const val FIRST_LAUNCH = "wallet_first_launch"
  }

  fun sendFirstLaunchEvent() {
    analyticsManager.logEvent(HashMap<String, Any>(), FIRST_LAUNCH, AnalyticsManager.Action.OPEN,
        WALLET)
  }
}