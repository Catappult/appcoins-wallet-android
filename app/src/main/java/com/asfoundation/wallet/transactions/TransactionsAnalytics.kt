package com.asfoundation.wallet.transactions

import cm.aptoide.analytics.AnalyticsManager
import com.asfoundation.wallet.abtesting.experiments.balancewallets.BalanceWalletsAnalytics
import java.util.*

class TransactionsAnalytics(private val analytics: AnalyticsManager,
                            private val balanceWalletsAnalytics: BalanceWalletsAnalytics) {

  companion object {
    const val WALLET_HOME_INTERACTION_EVENT = "wallet_home_interaction_event"
    const val CALL_TO_ACTION = "call_to_action"
    const val CARD_BUTTON_ACTION = "card_button_action"
    const val OPEN_APPLICATION = "OPEN_APPLICATION"

    private const val UNIQUE_NAME = "unique_name"
    private const val PACKAGE_NAME = "package_name"
    private const val WALLET = "WALLET"
  }

  fun openApp(uniqueName: String, packageName: String) {
    analytics.logEvent(
        hashMapOf<String, Any>(Pair(UNIQUE_NAME, uniqueName), Pair(PACKAGE_NAME, packageName)),
        OPEN_APPLICATION, AnalyticsManager.Action.OPEN, WALLET)
  }

  fun sendAbTestImpressionEvent(assignment: String) =
      balanceWalletsAnalytics.sendAbTestParticipatingEvent(assignment)

  fun sendAbTestConversionEvent() = balanceWalletsAnalytics.sendAbTestConvertingEvent()

  fun sendAction(action: String) {
    sendAction(action, null)
  }

  fun sendAction(action: String, button: String? = null) {
    val data = HashMap<String, Any>()
    data[CALL_TO_ACTION] = action

    if (action == "notification_card_button" && button != null) {
      data[CARD_BUTTON_ACTION] = button
    }
    analytics.logEvent(data, WALLET_HOME_INTERACTION_EVENT, AnalyticsManager.Action.CLICK, WALLET)
  }
}