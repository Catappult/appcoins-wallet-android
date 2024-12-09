package com.appcoins.wallet.core.utils.android_common

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.delay

object AppUtils {
  suspend fun restartApp(activity: Activity, copyIntent: Boolean = false) {
    delay(100)
    val packageManager: PackageManager = activity.packageManager
    val intent = packageManager.getLaunchIntentForPackage(activity.packageName)
    val componentName = intent!!.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    mainIntent.setPackage(activity.packageName)
    if (copyIntent) {
      val copiedIntent =
        if (activity is OnNewIntentActivityHandler) activity.getCurrentIntent() else activity.intent
      mainIntent.data = copiedIntent.data
      mainIntent.action = copiedIntent.action
      activity.intent.extras?.let { mainIntent.putExtras(it) }
    }
    activity.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
  }
}