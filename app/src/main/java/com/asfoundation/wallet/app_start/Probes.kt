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
        SOURCE to "",
        SKU to startMode.sku,
        BACKUP to startMode.backup
      )

      is StartMode.GPInstall -> mapOf(
        PACKAGE_NAME to startMode.packageName,
        SOURCE to startMode.source,
        SKU to startMode.sku,
      )

      is StartMode.RestoreGuestWalletFlow -> mapOf(
        BACKUP to startMode.backup,
        PACKAGE_NAME to startMode.packageName,
        SOURCE to "",
        SKU to startMode.sku,
      )

      else -> mapOf(PACKAGE_NAME to "", SOURCE to "", SKU to "")
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
    const val SOURCE = "source"
    const val SKU = "sku"
    const val BACKUP = "backup"
  }
}
