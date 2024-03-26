package com.asfoundation.wallet.ui.settings.entry

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class SettingsAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {
  private companion object {
    private const val WALLET = "WALLET"

    const val SETTINGS_CLICK_EVENT = "wallet_app_settings_click"
  }

  fun sendManageWalletScreenEvent(action: String) =
    sendActionEvent(action, SETTINGS_CLICK_EVENT)

  private fun sendActionEvent(action: String, event: String) {
    val data = HashMap<String, Any>()
    data["action"] = action
    analyticsManager.logEvent(data, event, AnalyticsManager.Action.CLICK, WALLET)
  }
}