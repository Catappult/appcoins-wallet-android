package com.appcoins.wallet.core.analytics.analytics.legacy

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class GetAppAnalytics @Inject constructor(
  private val analytics: AnalyticsManager
) {

  fun sendGetAppAEvent(packageName: String?, action: String) {
    val data = HashMap<String, Any>()
    packageName?.let{ data[PACKAGE_NAME] = packageName }
    data[GAME_ACTION] = action
    analytics.logEvent(
      data,
      WALLET_APP_ACTIVE_PROMOTION_CLICK,
      AnalyticsManager.Action.CLICK,
      WalletsAnalytics.WALLET
    )
  }

  companion object {
    const val WALLET_APP_ACTIVE_PROMOTION_CLICK = "wallet_app_active_promotion_click"
    const val PACKAGE_NAME = "package_name"
    const val GAME_ACTION = "game_action"
    const val GET_ACTION = "get"
    const val OPEN_ACTION = "open"
  }
}
