package com.asfoundation.wallet.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.*

class UserAgentInterceptor(private val context: Context,
                           private val preferencesRepositoryType: PreferencesRepositoryType) :
    Interceptor {

  private val userAgent: String
    get() {
      val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
      val display = wm.defaultDisplay
      val displayMetrics = DisplayMetrics()
      display.getRealMetrics(displayMetrics)
      val walletId = getOrCreateWalletId()
      val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
      return ("AppCoins_Wallet/"
          + BuildConfig.VERSION_NAME
          + " (Linux; Android "
          + Build.VERSION.RELEASE.replace(";".toRegex(), " ")
          + "; "
          + Build.VERSION.SDK_INT
          + "; "
          + Build.MODEL.replace(";".toRegex(), " ")
          + " Build/"
          + Build.PRODUCT.replace(";", " ")
          + "; "
          + System.getProperty("os.arch")
          + "; "
          + BuildConfig.APPLICATION_ID
          + "; "
          + BuildConfig.VERSION_CODE
          + "; "
          + walletId
          + "; "
          + displayMetrics.widthPixels
          + "x"
          + displayMetrics.heightPixels
          + "; "
          + androidId
          + ")")
    }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val requestWithUserAgent = originalRequest.newBuilder()
        .header("User-Agent", userAgent)
        .build()
    return chain.proceed(requestWithUserAgent)
  }

  private fun getOrCreateWalletId(): String {
    var walletId = preferencesRepositoryType.getWalletId()
    if (walletId == null) {
      val randomId = UUID.randomUUID()
          .toString()
      preferencesRepositoryType.setWalletId(randomId)
      walletId = randomId
    }
    return walletId
  }
}
