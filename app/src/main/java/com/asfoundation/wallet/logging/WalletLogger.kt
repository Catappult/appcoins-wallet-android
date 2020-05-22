package com.asfoundation.wallet.logging

class WalletLogger(private var logReceivers: ArrayList<LogReceiver>): Logger {

  override fun log(tag: String?, message: String?) {
    logReceivers.forEach { it.log(tag, message) }
  }

  override fun log(tag: String?, throwable: Throwable?) {
    logReceivers.forEach { it.log(tag, throwable) }
  }
  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    logReceivers.forEach { it.log(tag, message, throwable) }
  }

  override fun addReceiver(receiver: LogReceiver) {
    logReceivers.add(receiver)
  }

  override fun removeReceiver(receiver: LogReceiver) {
    logReceivers.remove(receiver)
  }
}