package com.appcoins.wallet.feature.backup.ui.triggers

import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource.TriggerSource
import com.google.gson.Gson

object TriggerUtils {
  fun TriggerSource.toJson(): String = Gson().toJson(this)
}