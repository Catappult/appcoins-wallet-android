package com.asfoundation.wallet.abtesting.experiments.balancewallets

import cm.aptoide.analytics.AnalyticsManager
import java.util.*

class BalanceWalletsAnalytics(private val analyticsManager: AnalyticsManager) {

  private var cachedAssignment = BalanceWalletsExperiment.NO_EXPERIMENT

  companion object {
    private const val WALLET = "WALLET"
    const val WAL_78_BALANCE_VS_MYWALLETS_PARTICIPATING_EVENT =
        "wallet_wal_78_Balance-vs-MyWallets_participating"
    const val WAL_78_BALANCE_VS_MYWALLETS_CONVERSION_EVENT =
        "wallet_wal_78_Balance-vs-MyWallets_converting"
  }

  fun sendAbTestParticipatingEvent(group: String) {
    if (group != BalanceWalletsExperiment.NO_EXPERIMENT) {
      cachedAssignment = group
      val data = HashMap<String, Any>()
      data["group"] = group
      analyticsManager.logEvent(data, WAL_78_BALANCE_VS_MYWALLETS_PARTICIPATING_EVENT,
          AnalyticsManager.Action.IMPRESSION, WALLET)
    }
  }

  fun sendAbTestConvertingEvent() {
    if (cachedAssignment != BalanceWalletsExperiment.NO_EXPERIMENT) {
      val data = HashMap<String, Any>()
      data["group"] = cachedAssignment
      analyticsManager.logEvent(data, WAL_78_BALANCE_VS_MYWALLETS_CONVERSION_EVENT,
          AnalyticsManager.Action.CLICK, WALLET)
    }
  }
}