package com.asfoundation.wallet.logging

import com.appcoins.wallet.core.utils.jvm_common.LogReceiver
import io.sentry.Sentry
import io.sentry.event.Breadcrumb
import io.sentry.event.BreadcrumbBuilder
import io.sentry.event.Event
import io.sentry.event.EventBuilder

class SentryReceiver : com.appcoins.wallet.core.utils.jvm_common.LogReceiver {

  override fun log(tag: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.capture(throwable)
    }
  }

  override fun log(tag: String?, message: String?, asError: Boolean, addToBreadcrumbs: Boolean) {
    message?.let {
      if (asError) {
        val errorEvent = EventBuilder()
        errorEvent.withLevel(Event.Level.ERROR)
        errorEvent.withMessage(it)
        Sentry.capture(errorEvent)
      } else {
        Sentry.capture("$tag: $message")
      }
      if (addToBreadcrumbs) {
        Sentry.getContext().recordBreadcrumb(
          BreadcrumbBuilder()
            .setType(Breadcrumb.Type.DEFAULT)
            .setLevel(Breadcrumb.Level.ERROR)
            .setMessage(tag)
            .setCategory(tag)
            .setData(mapOf(Pair("error", message)))
            .build()
        )
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