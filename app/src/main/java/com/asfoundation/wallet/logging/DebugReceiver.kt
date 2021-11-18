package com.asfoundation.wallet.logging

import com.appcoins.wallet.commons.LogReceiver
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.util.Log

class DebugReceiver : LogReceiver {

  override fun log(tag: String?, throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
      throwable?.printStackTrace()
      Log.e(tag?: "Logger", throwable?.message, throwable)
    }
  }

  override fun log(tag: String?, message: String?) {
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