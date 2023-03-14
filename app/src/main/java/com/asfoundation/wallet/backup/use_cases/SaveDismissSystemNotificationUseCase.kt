package com.asfoundation.wallet.backup.use_cases

import preferences.BackupSystemNotificationPreferencesDataSource
import javax.inject.Inject

class SaveDismissSystemNotificationUseCase @Inject constructor(
  private val backupSystemNotificationPreferences: BackupSystemNotificationPreferencesDataSource
) {

  operator fun invoke(walletAddress: String) {
    backupSystemNotificationPreferences.setDismissedBackupSystemNotificationSeenTime(walletAddress, System.currentTimeMillis())
  }
}