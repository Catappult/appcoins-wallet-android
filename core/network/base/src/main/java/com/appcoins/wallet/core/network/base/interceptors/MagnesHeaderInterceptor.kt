package com.appcoins.wallet.core.network.base.interceptors

import android.content.Context
import com.appcoins.wallet.core.network.base.MagnesUtils
import java.io.IOException
import okhttp3.*

class MagnesHeaderInterceptor(private val context: Context) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val magnesMetadataId = MagnesUtils.getMetadataId()
    val requestWithMagnesMetadataId: Request =
        if (!magnesMetadataId.isNullOrEmpty() &&
            originalRequest.url.toString().contains("/gateways/paypal")) {
          originalRequest.newBuilder().header("PayPal-Client-Metadata-Id", magnesMetadataId).build()
        } else {
          originalRequest.newBuilder().build()
        }
    return chain.proceed(requestWithMagnesMetadataId)
  }
}
