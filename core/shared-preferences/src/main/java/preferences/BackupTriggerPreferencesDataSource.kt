package preferences

import android.content.SharedPreferences
import com.google.gson.Gson
import javax.inject.Inject

class BackupTriggerPreferencesDataSource @Inject constructor(private val sharedPreferences: SharedPreferences) {
  fun setTriggerState(walletAddress: String, active: Boolean, triggerSource: TriggerSource) =
    sharedPreferences.edit()
      .putBoolean(BACKUP_TRIGGER_STATE + walletAddress, active)
      .putString(BACKUP_TRIGGER_SOURCE + walletAddress, Gson().toJson(triggerSource))
      .apply()

  fun getTriggerState(walletAddress: String) =
    sharedPreferences.getBoolean(BACKUP_TRIGGER_STATE + walletAddress, false)

  fun getTriggerSource(walletAddress: String): TriggerSource = Gson().fromJson(
    sharedPreferences.getString(
      BACKUP_TRIGGER_SOURCE + walletAddress,
      TriggerSource.NOT_SEEN.toString()
    ),
    TriggerSource::class.java
  )

  fun getBackupTriggerSeenTime(walletAddress: String) =
    sharedPreferences.getLong(BACKUP_SEEN_TIME + walletAddress, -1)

  fun setBackupTriggerSeenTime(walletAddress: String, currentTimeMillis: Long) =
    sharedPreferences.edit()
      .putLong(BACKUP_SEEN_TIME + walletAddress, currentTimeMillis)
      .apply()

  fun removeBackupTriggerSeenTime(walletAddress: String) =
    sharedPreferences.edit()
      .remove(BACKUP_SEEN_TIME + walletAddress)
      .apply()

  enum class TriggerSource {
    NEW_LEVEL, FIRST_PURCHASE, DISABLED, NOT_SEEN
  }

  companion object {
    private const val BACKUP_TRIGGER_STATE = "backup_trigger_state_"
    private const val BACKUP_TRIGGER_SOURCE = "backup_trigger_source_"
    private const val BACKUP_SEEN_TIME = "backup_seen_time_"
  }
}