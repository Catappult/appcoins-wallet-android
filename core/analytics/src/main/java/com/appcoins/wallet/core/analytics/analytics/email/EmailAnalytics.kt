package com.appcoins.wallet.core.analytics.analytics.email

import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.analytics.analytics.AnalyticsLabels.EVENT_STATUS
import javax.inject.Inject

class EmailAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  fun walletAppEmailSubmitted(status: String) {
    val data = HashMap<String, Any>()
    data[EVENT_STATUS] = status
    analyticsManager.logEvent(
      data,
      WALLET_APP_EMAIL_SUBMITTED,
      AnalyticsManager.Action.IMPRESSION,
      HOME
    )
  }

  fun walletAppEmailSubmitClick() {
    val data = HashMap<String, Any>()
    data[USER_ACTION] = USER_ACTION_VALUE
    analyticsManager.logEvent(
      data,
      WALLET_APP_HOME_SCREEN_CLICK,
      AnalyticsManager.Action.CLICK,
      HOME
    )
  }

  companion object {
    const val WALLET_APP_EMAIL_SUBMITTED = "wallet_app_email_submitted"
    const val WALLET_APP_HOME_SCREEN_CLICK = "wallet_app_home_screen_click"
    private const val HOME = "home"
    private const val USER_ACTION = "user_action"
    private const val USER_ACTION_VALUE = "submit"
  }
}
