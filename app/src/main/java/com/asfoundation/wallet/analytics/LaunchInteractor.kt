package com.asfoundation.wallet.analytics

import android.content.SharedPreferences

class LaunchInteractor(val launchAnalytics: LaunchAnalytics,
                       val sharedPreferences: SharedPreferences) {

  companion object {
    const val FIRST_LAUNCH_KEY = "first_launch"
  }

  fun sendFirstLaunchEvent() {
    if (sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true)) {
      launchAnalytics.sendFirstLaunchEvent()
      sharedPreferences.edit()
          .putBoolean(FIRST_LAUNCH_KEY, false)
          .apply()
    }
  }
}