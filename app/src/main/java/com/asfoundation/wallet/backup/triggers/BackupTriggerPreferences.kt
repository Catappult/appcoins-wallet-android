package com.asfoundation.wallet.backup.triggers

import android.content.SharedPreferences
import javax.inject.Inject

class BackupTriggerPreferences @Inject constructor(private val pref: SharedPreferences) {

  companion object {
    private const val BACKUP_TRIGGER_STATE = "backup_trigger_state"
    private const val BACKUP_TRIGGER_SOURCE = "backup_trigger_source"
  }

  fun setTriggerState(active: Boolean, triggerSource: String? = "Disabled") {
    pref.edit()
      .putBoolean(BACKUP_TRIGGER_STATE, active)
      .putString(BACKUP_TRIGGER_SOURCE, triggerSource)
      .apply()
  }

  fun getTriggerState(): Boolean {
    return pref.getBoolean(BACKUP_TRIGGER_STATE, false)
  }

  fun getTriggerSource(): String {
    return pref.getString(BACKUP_TRIGGER_SOURCE, "").toString()
  }

}