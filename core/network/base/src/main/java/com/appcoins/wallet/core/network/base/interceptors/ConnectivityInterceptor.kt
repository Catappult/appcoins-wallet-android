package com.appcoins.wallet.core.network.base.interceptors

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityInterceptor @Inject constructor(
  @param:ApplicationContext private val context: Context,
) : Interceptor {

  @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
  override fun intercept(chain: Interceptor.Chain): Response {
    if (!isConnected()) throw NoConnectivityException()
    return chain.proceed(chain.request())
  }

  @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
  private fun isConnected(): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}

class NoConnectivityException : IOException("No internet connection")