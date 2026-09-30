package com.asfoundation.wallet.logging

import com.appcoins.wallet.core.utils.jvm_common.LogReceiver
import com.appcoins.wallet.core.utils.jvm_common.TaggedThrowable
import io.sentry.Breadcrumb
import io.sentry.IScope
import io.sentry.Sentry
import io.sentry.SentryLevel

class SentryReceiver : LogReceiver {

  override fun log(tag: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.captureException(it) { scope -> scope.setThrowableTags(it) }
    }
  }

  override fun log(tag: String?, message: String?, asError: Boolean, addToBreadcrumbs: Boolean) {
    message?.let {
      if (asError) {
        Sentry.captureMessage(it) { scope ->
          scope.level = SentryLevel.WARNING
          if (tag != null) {
            scope.setTag("category", tag)
          }
        }
        if (addToBreadcrumbs) {
          Sentry.addBreadcrumb(
            Breadcrumb(it).apply {
              level = SentryLevel.WARNING
              if (tag != null) {
                category = tag
              }
            }
          )
        }
      } else {
        Sentry.addBreadcrumb(
          Breadcrumb(it).apply {
            level = SentryLevel.INFO
            if (tag != null) {
              category = tag
            }
          }
        )
      }
    }
  }

  override fun log(tag: String?, message: String?, throwable: Throwable?) {
    throwable?.let {
      Sentry.captureException(it) { scope ->
        tag?.let {
          scope.setTag("category", tag)
        }
        message?.let {
          scope.setExtra("error", message)
        }
        scope.setThrowableTags(it)
      }
    }
  }

  private fun IScope.setThrowableTags(throwable: Throwable) {
    (throwable as? TaggedThrowable)?.logTags?.forEach { (key, value) -> setTag(key, value) }
  }
}