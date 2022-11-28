package com.asfoundation.wallet.app_start

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject


class AppStartProbe @Inject constructor(
  private val analyticsManager: AnalyticsManager
) {

  operator fun invoke(startMode: StartMode) {
    if (startMode is StartMode.Subsequent) return
    val data = when (startMode) {
      is StartMode.PendingPurchaseFlow -> mapOf(
        PACKAGE_NAME to startMode.packageName,
        INTEGRATION_FLOW to startMode.integrationFlow,
        SOURCE to "",
        SKU to startMode.sku,
      )
      is StartMode.GPInstall -> mapOf(
        PACKAGE_NAME to startMode.packageName,
        INTEGRATION_FLOW to startMode.integrationFlow,
        SOURCE to startMode.source,
        SKU to startMode.sku,
      )
      else -> mapOf(PACKAGE_NAME to "", INTEGRATION_FLOW to "other", SOURCE to "", SKU to "")
    }
    analyticsManager.logEvent(
      data,
      FIRST_LAUNCH,
      AnalyticsManager.Action.OPEN,
      WALLET
    )
  }

  companion object {
    const val WALLET = "WALLET"
    const val FIRST_LAUNCH = "wallet_first_launch"
    const val PACKAGE_NAME = "package_name"
    const val INTEGRATION_FLOW = "integration_flow"
    const val SOURCE = "source"
    const val SKU = "sku"
  }
}
