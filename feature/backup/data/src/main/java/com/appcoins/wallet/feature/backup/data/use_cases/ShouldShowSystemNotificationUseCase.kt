package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.sharedpreferences.BackupSystemNotificationPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShouldShowSystemNotificationUseCase @Inject constructor(
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource,
  private val backupSystemNotificationPreferences: BackupSystemNotificationPreferencesDataSource
) {

  companion object {
    private const val PURCHASE_NOTIFICATION_THRESHOLD = 2
    private const val DISMISS_PERIOD = 30L
  }

  operator fun invoke(walletInfo: WalletInfo): Single<Boolean> = Single.just(
    walletInfo.hasBackup.not()
        && meetsLastDismissCondition(walletInfo.wallet)
        && meetsCountConditions(walletInfo.wallet)
  )

  private fun meetsLastDismissCondition(walletAddress: String): Boolean {
    val savedTime =
      backupSystemNotificationPreferences.getDismissedBackupSystemNotificationSeenTime(walletAddress)
    val currentTime = System.currentTimeMillis()
    return currentTime >= savedTime + TimeUnit.DAYS.toMillis(DISMISS_PERIOD)
  }

  private fun meetsCountConditions(walletAddress: String): Boolean {
    val count = commonsPreferencesDataSource.getWalletPurchasesCount(walletAddress)
    return count > 0 && count % PURCHASE_NOTIFICATION_THRESHOLD == 0
  }
}