package com.appcoins.wallet.core.network.base.interceptors

import com.appcoins.wallet.core.utils.jvm_common.LogReceiver
import com.appcoins.wallet.core.utils.jvm_common.Logger
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogInterceptorTest {

  @Test
  fun `error report sent to Sentry leaves out the query string and request body`() {
    val reports = mutableListOf<String?>()
    val interceptor = LogInterceptor().apply {
      logger = object : Logger {
        override fun log(tag: String?, message: String?, asError: Boolean, addToBreadcrumbs: Boolean) {
          reports += message
        }
        override fun log(tag: String?, throwable: Throwable?) = Unit
        override fun log(tag: String?, message: String?, throwable: Throwable?) = Unit
        override fun addReceiver(receiver: LogReceiver) = Unit
        override fun removeReceiver(receiver: LogReceiver) = Unit
      }
    }
    val json = "application/json".toMediaType()
    val client = OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .addInterceptor { chain ->
        Response.Builder()
          .request(chain.request())
          .protocol(Protocol.HTTP_1_1)
          .code(500)
          .message("Server Error")
          .body("""{"code":"BACKEND_ERROR"}""".toResponseBody(json))
          .build()
      }
      .build()
    val request = Request.Builder()
      .url("https://api.example.com/transactions?wallet.signature=SECRET_SIGNATURE")
      .post("""{"email":"SECRET_EMAIL"}""".toRequestBody(json))
      .build()

    client.newCall(request).execute().use { assertEquals(500, it.code) }

    val report = reports.single()!!
    assertTrue(report, report.contains("HTTP 500 POST https://api.example.com/transactions"))
    assertTrue(report, report.contains("BACKEND_ERROR"))
    assertFalse(report, report.contains("SECRET_SIGNATURE"))
    assertFalse(report, report.contains("SECRET_EMAIL"))
  }
}
