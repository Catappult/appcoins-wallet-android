package com.appcoins.wallet.core.utils.android_common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.delay

class AppUtils {
  companion object {
    suspend fun restartApp(context: Context) {
      delay(100)
      val packageManager: PackageManager = context.packageManager
      val intent = packageManager.getLaunchIntentForPackage(context.packageName)
      val componentName = intent!!.component
      val mainIntent = Intent.makeRestartActivityTask(componentName)
      mainIntent.setPackage(context.packageName)
      context.startActivity(mainIntent)
      Runtime.getRuntime().exit(0)
    }
  }
}