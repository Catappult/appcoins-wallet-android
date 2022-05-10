package com.asfoundation.wallet.backup.repository.preferences

import android.content.SharedPreferences
import com.google.gson.Gson
import io.reactivex.Completable
import javax.inject.Inject

class BackupTriggerPreferences @Inject constructor(private val pref: SharedPreferences) {
  companion object {
    private const val BACKUP_TRIGGER_STATE = "backup_trigger_state_"
    private const val BACKUP_TRIGGER_SOURCE = "backup_trigger_source_"
    private const val BACKUP_SEEN_TIME = "backup_seen_time_"
  }

  fun setTriggerState(walletAddress: String, active: Boolean, triggerSource: TriggerSource) {
    pref.edit()
      .putBoolean(BACKUP_TRIGGER_STATE + walletAddress, active)
      .putString(BACKUP_TRIGGER_SOURCE + walletAddress, Gson().toJson(triggerSource))
      .apply()
  }

  fun getTriggerState(walletAddress: String): Boolean {
    return pref.getBoolean(BACKUP_TRIGGER_STATE + walletAddress, false)
  }

  fun getTriggerSource(walletAddress: String): TriggerSource {
    return Gson().fromJson(
      pref.getString(BACKUP_TRIGGER_SOURCE + walletAddress, TriggerSource.NOT_SEEN.toString()),
      TriggerSource::class.java
    )
  }

  fun getBackupTriggerSeenTime(walletAddress: String) =
    pref.getLong(BACKUP_SEEN_TIME + walletAddress, -1)

  fun setBackupTriggerSeenTime(walletAddress: String, currentTimeMillis: Long) {
    pref.edit()
      .putLong(BACKUP_SEEN_TIME + walletAddress, currentTimeMillis)
      .apply()
  }

  fun removeBackupTriggerSeenTime(walletAddress: String): Completable {
    return Completable.fromAction {
      pref.edit()
        .remove(BACKUP_SEEN_TIME + walletAddress)
        .apply()
    }
  }

  enum class TriggerSource {
    NEW_LEVEL, FIRST_PURCHASE, DISABLED, NOT_SEEN
  }
}