package com.appcoins.wallet.core.analytics.analytics.logging

import com.appcoins.wallet.commons.LogReceiver
import com.appcoins.wallet.core.analytics.BuildConfig

class DebugReceiver : LogReceiver {

  override fun log(tag: String?, throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
      throwable?.printStackTrace()
      Log.e(tag?: "Logger", throwable?.message, throwable)
    }
  }

  override fun log(tag: String?, message: String?, asError: Boolean, addToBreadcrumbs: Boolean) {
    if (BuildConfig.DEBUG && message != null) {
      Log.e(tag ?: "Logger", message)
    }

  }

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
      Log.e(tag?: "Logger", message, throwable)
    }
  }


}