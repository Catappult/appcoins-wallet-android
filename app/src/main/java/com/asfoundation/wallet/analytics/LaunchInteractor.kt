package com.asfoundation.wallet.analytics

import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.asf.wallet.BuildConfig
import javax.inject.Inject

class LaunchInteractor @Inject constructor(private val launchAnalytics: LaunchAnalytics,
                                           private val sharedPreferences: SharedPreferences,
                                           private val packageManager: PackageManager) {

  companion object {
    const val FIRST_LAUNCH_KEY = "first_launch"
  }

  fun sendFirstLaunchEvent() {
    if (isFirstInstall() && sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true)) {
      launchAnalytics.sendFirstLaunchEvent()
      sharedPreferences.edit()
          .putBoolean(FIRST_LAUNCH_KEY, false)
          .apply()
    }
  }

  private fun isFirstInstall(): Boolean {
    return try {
      val firstInstallTime: Long =
          packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).firstInstallTime
      val lastUpdateTime: Long =
          packageManager.getPackageInfo(BuildConfig.APPLICATION_ID, 0).lastUpdateTime
      firstInstallTime == lastUpdateTime
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
      true
    }
  }
}