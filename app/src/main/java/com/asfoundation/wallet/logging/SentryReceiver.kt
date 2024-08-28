package com.asfoundation.wallet.logging

import com.appcoins.wallet.core.utils.jvm_common.LogReceiver
import io.sentry.Sentry
import io.sentry.SentryLevel

class SentryReceiver : LogReceiver {

  override fun log(tag: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.captureException(it)
    }
  }

  override fun log(tag: String?, message: String?, asError: Boolean, addToBreadcrumbs: Boolean) {
    message?.let {
      if (asError) {
        Sentry.captureMessage(it) { scope ->
          scope.level = SentryLevel.ERROR
          if (tag != null) {
            scope.setTag("category", tag)
          }
        }
      } else {
        Sentry.captureMessage("$tag: $message")
      }

      if (addToBreadcrumbs) {
        Sentry.captureMessage(tag ?: "Breadcrumb") { scope ->
          scope.level = SentryLevel.ERROR
          scope.setExtra("error", message)
          if (tag != null) {
            scope.setTag("category", tag)
          }
        }
      }}
  }

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.captureException(it)
    }
  }
}