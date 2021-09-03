package com.asfoundation.wallet.support

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedBroadcastReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      AlarmManagerBroadcastReceiver.scheduleAlarm(context)
    }
  }

}