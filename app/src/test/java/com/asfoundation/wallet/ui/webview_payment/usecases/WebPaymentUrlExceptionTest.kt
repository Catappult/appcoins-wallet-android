package com.asfoundation.wallet.ui.webview_payment.usecases

import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.SocketTimeoutException

class WebPaymentUrlExceptionTest {

  @Test
  fun `wraps error with the failing step`() {
    val cause = SocketTimeoutException("timeout")

    Single.error<String>(cause)
      .webPaymentUrlStep("country_code")
      .test()
      .assertError {
        it is WebPaymentUrlException && it.step == "country_code" && it.cause === cause
      }
  }

  @Test
  fun `keeps the original step when wrapped again`() {
    val error = Single.error<String>(IllegalStateException())
      .webPaymentUrlStep("ewt")
      .webPaymentUrlStep("assemble_url")
      .test()
      .errors()
      .single() as WebPaymentUrlException

    assertEquals("ewt", error.step)
  }

  @Test
  fun `exposes the step as a log tag`() {
    val exception = WebPaymentUrlException("cloud_ip", RuntimeException())

    assertEquals(mapOf(WebPaymentUrlException.STEP_TAG to "cloud_ip"), exception.logTags)
  }

  @Test
  fun `passes values through untouched`() {
    val value = "value"

    Single.just(value)
      .webPaymentUrlStep("promo_code")
      .test()
      .assertValue { it === value }
      .assertNoErrors()
  }
}
