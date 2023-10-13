package com.asfoundation.wallet.logging

import com.appcoins.wallet.core.utils.jvm_common.LogReceiver
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryItemType
import io.sentry.SentryLevel

class SentryReceiver : LogReceiver {

  override fun log(tag: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.captureException(throwable)
    }
  }

  override fun log(tag: String?, message: String?, asError: Boolean, addToBreadcrumbs: Boolean) {
    message?.let {
      if (asError) {
        Sentry.captureMessage(it, SentryLevel.ERROR)
      } else {
        Sentry.captureMessage("$tag: $message")
      }
      if (addToBreadcrumbs) {
        Sentry.addBreadcrumb(
          Breadcrumb(message).apply {
            type = SentryItemType.Event.itemType
            level = SentryLevel.ERROR
            category = tag
          }
        )
      }
    }
  }

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.captureMessage("$tag: $message")
      Sentry.captureException(it)
    }
  }
}