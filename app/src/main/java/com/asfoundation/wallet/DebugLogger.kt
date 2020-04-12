package com.asfoundation.wallet

import android.util.Log
import com.asf.wallet.BuildConfig

class DebugLogger : Logger {
  override fun log(throwable: Throwable) {
    if (BuildConfig.DEBUG) {
      throwable.printStackTrace()
      Log.d("Logger", throwable.message)
    }
  }

  override fun log(message: String) {
    log(Throwable(message))
  }


}