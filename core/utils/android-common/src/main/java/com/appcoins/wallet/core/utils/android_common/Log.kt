package com.appcoins.wallet.core.utils.android_common

import android.util.Log

class Log private constructor() {

  companion object {

    const val VERBOSE = Log.VERBOSE

    const val DEBUG = Log.DEBUG

    const val INFO = Log.INFO

    const val WARN = Log.WARN

    const val ERROR = Log.ERROR

    const val ASSERT = Log.ASSERT

    @JvmStatic
    fun i(tag: String, msg: String): Int =
        if (BuildConfig.DEBUG) Log.i(tag, msg) else 0

    @JvmStatic
    fun d(tag: String, msg: String): Int =
        if (BuildConfig.DEBUG) Log.d(tag, msg) else 0

    @JvmStatic
    fun d(tag: String, msg: String, throwable: Throwable): Int =
        if (BuildConfig.DEBUG) Log.d(tag, msg, throwable) else 0

    @JvmStatic
    fun w(tag: String, msg: String): Int =
        if (BuildConfig.DEBUG) Log.w(tag, msg) else 0

    // We are not filtering the error logs since they are usually used only in extreme situation
    // and there shouldn't be much arm done logging them.
    @JvmStatic
    fun e(tag: String, msg: String?, throwable: Throwable?): Int = Log.e(tag, msg, throwable)

    @JvmStatic
    fun e(tag: String, msg: String): Int = Log.e(tag, msg)
  }
}