package com.appcoins.wallet.core.network.base.interceptors

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.*

class UserAgentInterceptor(
  private val context: Context,
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource
) :
  Interceptor {
    val userAgent: String
    get() {
      val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
      val display = wm.defaultDisplay
      val displayMetrics = DisplayMetrics()
      display.getRealMetrics(displayMetrics)
      val walletId = getOrCreateWalletId()
      val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
      val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

      return ("AppCoins_Wallet/"
          + packageInfo.versionName
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
          + context.packageName
          + "; "
          + packageInfo.versionCode
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
    val response = chain.proceed(requestWithUserAgent)

    // Overrides 204 response with 200 to avoid retrofit crash
    if (response.code == 204 && originalRequest.url.toUrl().path.toString()
        .contains("appc/guest_wallet/cached_values")
    ) {
      return response.newBuilder()
        .code(200)
        .body("{\"private_key\":\"\"}".toResponseBody("application/json".toMediaTypeOrNull()))
        .build()
    }
    return response
  }

  private fun getOrCreateWalletId(): String {
    var walletId = commonsPreferencesDataSource.getWalletId()
    if (walletId == null) {
      val randomId = UUID.randomUUID()
        .toString()
      commonsPreferencesDataSource.setWalletId(randomId)
      walletId = randomId
    }
    return walletId
  }
}

class NoContentException(override val message: String) : Throwable()
class ResetContentException(override val message: String) : Throwable()
