package com.asfoundation.wallet.logging

import com.appcoins.wallet.commons.LogReceiver
import io.sentry.Sentry
import io.sentry.event.Event
import io.sentry.event.EventBuilder

class SentryReceiver : LogReceiver {

  override fun log(tag: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.capture(throwable)
    }
  }

  override fun log(tag: String?, message: String?, asError: Boolean) {
    message?.let {
      if (asError) {
        val errorEvent = EventBuilder()
        errorEvent.withLevel(Event.Level.ERROR)
        errorEvent.withMessage(it)
        Sentry.capture(errorEvent)
      } else {
        Sentry.capture("$tag: $message")
      }
    }
  }

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.capture("$tag: $message")
      Sentry.capture(it)
    }
  }
}