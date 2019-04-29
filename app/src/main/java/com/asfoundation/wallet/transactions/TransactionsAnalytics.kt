package com.asfoundation.wallet.transactions

import cm.aptoide.analytics.AnalyticsManager

class TransactionsAnalytics(private val analytics: AnalyticsManager) {
  companion object {
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
}