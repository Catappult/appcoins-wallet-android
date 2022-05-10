package com.asfoundation.wallet.backup.use_cases

import com.asfoundation.wallet.backup.repository.preferences.BackupSystemNotificationPreferences
import javax.inject.Inject

class SaveDismissSystemNotificationUseCase @Inject constructor(
  private val backupSystemNotificationPreferences: BackupSystemNotificationPreferences
) {

  operator fun invoke(walletAddress: String) {
    backupSystemNotificationPreferences.setDismissedBackupSystemNotification(walletAddress)
  }
}