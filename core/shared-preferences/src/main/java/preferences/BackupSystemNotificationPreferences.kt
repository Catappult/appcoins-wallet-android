package preferences

import android.content.SharedPreferences
import javax.inject.Inject

class BackupSystemNotificationPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {
  companion object {
    private const val BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME = "backup_system_notification_seen_time_"
  }

  fun getDismissedBackupSystemNotificationSeenTime(walletAddress: String) =
    sharedPreferences.getLong(BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME + walletAddress, -1)

  fun setDismissedBackupSystemNotificationSeenTime(walletAddress: String, currentTimeMillis: Long) =
    sharedPreferences.edit()
      .putLong(BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME + walletAddress, currentTimeMillis)
      .apply()

  fun removeDismissedBackupSystemNotificationSeenTime(walletAddress: String) =
    sharedPreferences.edit()
      .remove(BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME + walletAddress)
      .apply()
}