package com.appcoins.wallet.core.utils.jvm_common

interface LogReceiver {
  companion object {
    const val DEFAULT_TAG = "default_tag"
    const val DEFAULT_MSG = "default_message"
    const val DEFAULT_THROWABLE_MSG = "default_throwable_msg"
    const val DEFAULT_THROWABLE_STATCKTRACE = "default_throwable_stacktrace"
  }
  fun log(tag: String?, throwable: Throwable?)
  fun log(tag: String?, message: String?, asError: Boolean = false, addToBreadcrumbs: Boolean = false)
  fun log(tag: String?, message: String?, throwable: Throwable?)
}