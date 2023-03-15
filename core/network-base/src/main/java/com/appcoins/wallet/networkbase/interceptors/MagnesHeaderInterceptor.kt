package com.appcoins.wallet.networkbase.interceptors

import android.content.Context
import com.appcoins.wallet.networkbase.MagnesUtils
import okhttp3.*
import java.io.IOException

class MagnesHeaderInterceptor(
  private val context: Context
) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val magnesMetadataId = MagnesUtils.getMetadataId()
    val requestWithMagnesMetadataId: Request = if (
      !magnesMetadataId.isNullOrEmpty() &&
      originalRequest.url.toString().contains("/gateways/paypal")
        ) {
      originalRequest.newBuilder()
        .header("PayPal-Client-Metadata-Id", magnesMetadataId)
        .build()
    } else {
      originalRequest.newBuilder()
        .build()
    }
    return chain.proceed(requestWithMagnesMetadataId)
  }

}
