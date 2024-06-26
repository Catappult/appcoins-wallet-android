package com.appcoins.wallet.core.utils.jvm_common

interface Logger {
  fun log(
    tag: String?,
    message: String?,
    asError: Boolean = false,
    addToBreadcrumbs: Boolean = false
  )

  fun log(tag: String?, throwable: Throwable?)
  fun log(tag: String?, message: String?, throwable: Throwable?)
  fun addReceiver(receiver: LogReceiver)
  fun removeReceiver(receiver: LogReceiver)
}