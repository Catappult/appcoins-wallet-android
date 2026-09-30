package com.appcoins.wallet.core.utils.jvm_common

/**
 * Implemented by throwables that carry tags to attach when they are logged (e.g. as Sentry tags).
 * Keep values to a small, fixed set so they stay useful for searching and grouping.
 */
interface TaggedThrowable {
  val logTags: Map<String, String>
}
