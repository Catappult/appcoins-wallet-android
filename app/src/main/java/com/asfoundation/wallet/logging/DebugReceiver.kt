package com.asfoundation.wallet.logging

import com.appcoins.wallet.core.utils.jvm_common.LogReceiver
import com.asf.wallet.BuildConfig
import com.appcoins.wallet.core.utils.android_common.Log

class DebugReceiver : com.appcoins.wallet.core.utils.jvm_common.LogReceiver {

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