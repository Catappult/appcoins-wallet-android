package com.asfoundation.wallet.backup.repository.preferences

import android.content.SharedPreferences
import io.reactivex.Completable
import javax.inject.Inject

class BackupSystemNotificationPreferences @Inject constructor(private val pref: SharedPreferences) {

  companion object {
    private const val BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME = "backup_system_notification_seen_time_"
  }

  fun getDismissedBackupSystemNotificationSeenTime(walletAddress: String) =
    pref.getLong(BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME + walletAddress, -1)

  fun setDismissedBackupSystemNotificationSeenTime(walletAddress: String, currentTimeMillis: Long) {
    pref.edit()
      .putLong(BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME + walletAddress, currentTimeMillis)
      .apply()
  }

  fun removeDismissedBackupSystemNotificationSeenTime(walletAddress: String): Completable {
    return Completable.fromAction {
      pref.edit()
        .remove(BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME + walletAddress)
        .apply()
    }
  }
}