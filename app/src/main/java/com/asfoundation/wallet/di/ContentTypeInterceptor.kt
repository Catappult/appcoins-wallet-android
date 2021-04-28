package com.asfoundation.wallet.di

import okhttp3.Request

class ContentTypeInterceptor : okhttp3.Interceptor {
  override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
    val original: Request = chain.request()

    val requestBuilder: Request.Builder = original.newBuilder()
        .addHeader("Content-Type",
            "application/json;format=product_token") // <-- this is the important line

    val request: Request = requestBuilder.build()
    return chain.proceed(request)
  }
}