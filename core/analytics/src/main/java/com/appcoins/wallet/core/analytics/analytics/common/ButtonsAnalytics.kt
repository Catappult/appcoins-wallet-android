package com.appcoins.wallet.core.analytics.analytics.common

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class ButtonsAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager?) {

  fun sendDefaultButtonClickAnalytics(fragmentName: String, buttonName: String) {
    val eventData = HashMap<String, Any>()
    eventData[USER_ACTION] = buttonName
    eventData[CONTEXT] = fragmentName
    analyticsManager?.logEvent(
      eventData, WALLET_APP_CLICK,
      AnalyticsManager.Action.CLICK, WALLET
    )
  }


  companion object {
    const val WALLET_APP_CLICK = "wallet_app_click"
    const val USER_ACTION = "user_action"
    const val CONTEXT = "context"
    private const val WALLET = "wallet"

  }
}
