package com.asfoundation.wallet.manage_wallets

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class ManageWalletAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {
  private companion object {
    private const val WALLET = "WALLET"

    const val MANAGE_WALLET_EVENT = "wallet_app_manage_wallet_impression"
    const val MANAGE_WALLET_CLICK_EVENT = "wallet_app_manage_wallet_click"
    const val MANAGE_WALLET_PICKER_CLICK_EVENT = "wallet_app_payment_method_to_verify_click"
  }

  fun sendManageWalletScreenEvent() = sendScreenEvent(MANAGE_WALLET_EVENT)

  fun sendManageWalletScreenEvent(action: String) =
    sendActionEvent(action, MANAGE_WALLET_CLICK_EVENT)

  fun sendManageWalletPickerScreenEvent(action: String) =
    sendActionEvent(action, MANAGE_WALLET_PICKER_CLICK_EVENT)

  private fun sendActionEvent(action: String, event: String) {
    val data = HashMap<String, Any>()
    data["action"] = action
    analyticsManager.logEvent(data, event, AnalyticsManager.Action.CLICK, WALLET)
  }

  private fun sendScreenEvent(event: String) {
    analyticsManager.logEvent(mapOf(), event, AnalyticsManager.Action.IMPRESSION, WALLET)
  }
}