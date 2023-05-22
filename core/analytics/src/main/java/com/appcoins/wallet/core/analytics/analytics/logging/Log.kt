package com.appcoins.wallet.core.analytics.analytics.logging

import android.util.Log
import com.appcoins.wallet.core.analytics.BuildConfig

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
        if (BuildConfig.BUILD_TYPE != "release") Log.i(tag, msg) else 0

    @JvmStatic
    fun d(tag: String, msg: String): Int =
        if (BuildConfig.BUILD_TYPE != "release") Log.d(tag, msg) else 0

    @JvmStatic
    fun d(tag: String, msg: String, throwable: Throwable): Int =
        if (BuildConfig.BUILD_TYPE != "release") Log.d(tag, msg, throwable) else 0

    @JvmStatic
    fun w(tag: String, msg: String): Int =
        if (BuildConfig.BUILD_TYPE != "release") Log.w(tag, msg) else 0

    // We are not filtering the error logs since they are usually used only in extreme situation
    // and there shouldn't be much arm done logging them.
    @JvmStatic
    fun e(tag: String, msg: String?, throwable: Throwable?): Int = Log.e(tag, msg, throwable)

    @JvmStatic
    fun e(tag: String, msg: String): Int = Log.e(tag, msg)
  }
}