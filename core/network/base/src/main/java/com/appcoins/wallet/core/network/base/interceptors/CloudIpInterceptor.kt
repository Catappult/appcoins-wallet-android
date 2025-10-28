package com.appcoins.wallet.core.network.base.interceptors

import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class CloudIpInterceptor @Inject constructor(
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val cloudIp = commonsPreferencesDataSource.getCloudIp()
    return if (!cloudIp.isNullOrBlank())
      chain.proceed(
      chain.request()
        .newBuilder()
        .header("x-client-ip", cloudIp)
        .build()
    )
    else
      chain.proceed(chain.request())
  }
}
