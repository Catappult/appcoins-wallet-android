package com.asfoundation.wallet.permissions

import android.content.Context
import android.graphics.drawable.Drawable

class AndroidAppDataProvider(private val context: Context) {
  fun getAppInfo(packageName: String): ApplicationInfo {
    val packageInfo = context.packageManager.getApplicationInfo(packageName, 0)
    val appName = context.packageManager.getApplicationLabel(packageInfo)
    val icon = context.packageManager
        .getApplicationIcon(packageName)
    return ApplicationInfo(packageName, appName, icon)
  }

  data class ApplicationInfo(val packageName: String, val appName: CharSequence,
                             val icon: Drawable)

}
