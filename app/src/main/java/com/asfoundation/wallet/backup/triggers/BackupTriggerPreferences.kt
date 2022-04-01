package com.asfoundation.wallet.backup.triggers

import android.content.SharedPreferences
import javax.inject.Inject

class BackupTriggerPreferences @Inject constructor(private val pref: SharedPreferences) {

  companion object {
    private const val BACKUP_TRIGGER_STATE = "backup_trigger_state"
  }

  fun setTriggerState(active: Boolean) {
    pref.edit()
      .putBoolean(BACKUP_TRIGGER_STATE, active)
      .apply()
  }

  fun getTriggerState(): Boolean {
    return pref.getBoolean(BACKUP_TRIGGER_STATE, false)
  }
}