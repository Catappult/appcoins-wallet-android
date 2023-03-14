package com.asfoundation.wallet.backup.use_cases

import preferences.BackupSystemNotificationPreferences
import javax.inject.Inject

class SaveDismissSystemNotificationUseCase @Inject constructor(
  private val backupSystemNotificationPreferences: BackupSystemNotificationPreferences
) {

  operator fun invoke(walletAddress: String) {
    backupSystemNotificationPreferences.setDismissedBackupSystemNotificationSeenTime(walletAddress, System.currentTimeMillis())
  }
}