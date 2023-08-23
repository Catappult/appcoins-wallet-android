package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.sharedpreferences.BackupSystemNotificationPreferencesDataSource
import javax.inject.Inject

class SaveDismissSystemNotificationUseCase @Inject constructor(
  private val backupSystemNotificationPreferences: BackupSystemNotificationPreferencesDataSource
) {

  operator fun invoke(walletAddress: String) {
    backupSystemNotificationPreferences.setDismissedBackupSystemNotificationSeenTime(walletAddress, System.currentTimeMillis())
  }
}