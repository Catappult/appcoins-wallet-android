package com.asfoundation.wallet.util

import android.util.Log
import com.asf.wallet.BuildConfig

class Log private constructor() {

  companion object {
    @JvmStatic
    fun i(tag: String, msg: String): Int = if (BuildConfig.LOGGABLE) Log.i(tag, msg) else 0

    @JvmStatic
    fun d(tag: String, msg: String): Int = if (BuildConfig.LOGGABLE) Log.d(tag, msg) else 0

    @JvmStatic
    fun d(tag: String, msg: String, throwable: Throwable): Int =
        if (BuildConfig.LOGGABLE) Log.d(tag, msg, throwable) else 0
  }
}