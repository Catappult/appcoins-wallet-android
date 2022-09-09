package com.asfoundation.wallet.app_start

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject


class AppStartProbe @Inject constructor(
  private val analyticsManager: AnalyticsManager
) {

  operator fun invoke(startMode: StartMode) {
    if (startMode is StartMode.Subsequent) return
    val utmData = if (startMode is StartMode.FirstUtm) startMode else null
    analyticsManager.logEvent(
      mapOf(
        PACKAGE_NAME to (utmData?.packageName ?: ""),
        INTEGRATION_FLOW to (utmData?.integrationFlow ?: ""),
        SOURCE to (utmData?.source ?: ""),
        SKU to (utmData?.sku ?: ""),
      ),
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
