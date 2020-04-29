package com.asfoundation.wallet.logging

import io.sentry.Sentry

class SentryReceiver : LogReceiver {

  override fun log(tag: String?, throwable: Throwable?) {
    throwable?.let {
      throwable.printStackTrace()
      Sentry.capture(throwable)
    }
  }

  override fun log(tag: String?, message: String?) {
    message?.let {
      Sentry.capture(message)
    }
  }

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.capture(it)
    }
  }
}