package com.appcoins.wallet.core.analytics.analytics.compatible_apps

import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import javax.inject.Inject

class CompatibleAppsAnalytics @Inject constructor(
  private val analytics: AnalyticsManager
) {

  fun sendPromotionClickEvent(packageName: String?, action: String) {
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
  }
}
