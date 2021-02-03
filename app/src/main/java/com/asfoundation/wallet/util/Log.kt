package com.asfoundation.wallet.util

import android.util.Log

class Log private constructor() {

  companion object {
    @JvmStatic
    fun i(tag: String, msg: String): Int = Log.i(tag, msg)
    @JvmStatic
    fun d(tag: String, msg: String): Int = Log.d(tag, msg)
    @JvmStatic
    fun d(tag: String, msg: String, throwable: Throwable): Int = Log.d(tag, msg, throwable)
  }
}