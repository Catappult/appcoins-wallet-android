package com.asfoundation.wallet.wallet_v3.home

import cm.aptoide.analytics.AnalyticsManager
import java.util.*
import javax.inject.Inject

class HomeAnalytics @Inject constructor(private val analytics: AnalyticsManager) {

  companion object {
    const val WALLET_HOME_INTERACTION_EVENT = "wallet_home_interaction_event"
    const val CALL_TO_ACTION = "call_to_action"
    const val OPEN_APPLICATION = "OPEN_APPLICATION"

    private const val UNIQUE_NAME = "unique_name"
    private const val PACKAGE_NAME = "package_name"
    private const val WALLET = "WALLET"
  }

  fun openApp(uniqueName: String, packageName: String) {
    analytics.logEvent(
        hashMapOf<String, Any>(Pair(UNIQUE_NAME, uniqueName), Pair(PACKAGE_NAME, packageName)),
        OPEN_APPLICATION, AnalyticsManager.Action.OPEN, WALLET
    )
  }

  fun sendAction(action: String) {
    val data = HashMap<String, Any>()
    data[CALL_TO_ACTION] = action
    analytics.logEvent(data, WALLET_HOME_INTERACTION_EVENT, AnalyticsManager.Action.CLICK, WALLET)
  }
}