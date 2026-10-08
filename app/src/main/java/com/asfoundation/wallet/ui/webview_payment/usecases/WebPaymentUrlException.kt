package com.asfoundation.wallet.ui.webview_payment.usecases

import com.appcoins.wallet.core.utils.jvm_common.TaggedThrowable
import io.reactivex.Single

/**
 * Identifies which step of the web payment URL build failed, so it can be told apart in Sentry.
 */
class WebPaymentUrlException(val step: String, cause: Throwable) :
  Exception("Web payment URL build failed at step '$step': ${cause.message}", cause),
  TaggedThrowable {

  override val logTags: Map<String, String> = mapOf(STEP_TAG to step)

  companion object {
    const val STEP_TAG = "web_payment_url_step"
  }
}

internal fun <T : Any> Single<T>.webPaymentUrlStep(step: String): Single<T> =
  onErrorResumeNext { throwable: Throwable ->
    Single.error(
      throwable as? WebPaymentUrlException ?: WebPaymentUrlException(step, throwable)
    )
  }
