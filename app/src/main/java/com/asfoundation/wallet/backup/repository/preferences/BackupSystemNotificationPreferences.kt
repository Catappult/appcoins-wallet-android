package com.asfoundation.wallet.backup.repository.preferences

import android.content.SharedPreferences
import javax.inject.Inject

class BackupSystemNotificationPreferences @Inject constructor(private val pref: SharedPreferences) {

  companion object {
    private const val SEEN_BACKUP_SYSTEM_NOTIFICATION = "seen_backup_system_notification_"
  }

  fun hasDismissedBackupSystemNotification(walletAddress: String) =
    pref.getBoolean(SEEN_BACKUP_SYSTEM_NOTIFICATION + walletAddress, false)

  fun setDismissedBackupSystemNotification(walletAddress: String) =
    pref.edit()
      .putBoolean(SEEN_BACKUP_SYSTEM_NOTIFICATION + walletAddress, true)
      .apply()
}