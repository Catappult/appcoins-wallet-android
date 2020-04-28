package com.asfoundation.wallet.logging

interface Logger {
  fun log(tag: String?, message: String?)
  fun log(tag: String?, throwable: Throwable?)
  fun log(tag: String?, message: String?, throwable: Throwable?)
  fun addReceiver(receiver: LogReceiver)
  fun removeReceiver(receiver: LogReceiver)
}