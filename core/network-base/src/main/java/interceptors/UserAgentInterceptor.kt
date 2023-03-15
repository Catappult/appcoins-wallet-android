package interceptors

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import okhttp3.*
import java.io.IOException
import java.util.*

class UserAgentInterceptor(
  private val context: Context,
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource
) :
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
    val response = chain.proceed(requestWithUserAgent)

    /*
      // Throw specific Exceptions on HTTP 204 and HTTP 205 response codes, since Retrofit can't handle them
      // see retrofit issue: https://github.com/square/retrofit/issues/2867
      if (response.code == 204) throw NoContentException("HTTP 204. There is no content")
      if (response.code == 205) throw ResetContentException("HTTP 205. The content was reset")
    */
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
