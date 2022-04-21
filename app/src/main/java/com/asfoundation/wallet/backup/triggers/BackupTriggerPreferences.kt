package com.asfoundation.wallet.backup.triggers

import android.content.SharedPreferences
import com.google.gson.Gson
import javax.inject.Inject


class BackupTriggerPreferences @Inject constructor(private val pref: SharedPreferences) {

  fun setTriggerState(walletAddress: String, active: Boolean, triggerSource: TriggerSource) {
    pref.edit()
      .putBoolean("backup_trigger_state$walletAddress", active)
      .putString("backup_trigger_source$walletAddress", Gson().toJson(triggerSource))
      .apply()
  }

  fun getTriggerState(walletAddress: String): Boolean {
    return pref.getBoolean("backup_trigger_state$walletAddress", false)
  }

  fun getTriggerSource(walletAddress: String): TriggerSource {
    return Gson().fromJson(
      pref.getString("backup_trigger_source$walletAddress", TriggerSource.NOT_SEEN.toString()),
      TriggerSource::class.java
    )
  }

  enum class TriggerSource {
    NEW_LEVEL, FIRST_PURCHASE, DISABLED, NOT_SEEN
  }
}