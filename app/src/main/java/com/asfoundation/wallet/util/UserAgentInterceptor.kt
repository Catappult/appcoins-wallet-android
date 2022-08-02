package com.asfoundation.wallet.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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

//  @Throws(IOException::class)
//  override fun intercept(chain: Interceptor.Chain): Response {
//    val originalRequest = chain.request()
//    val requestWithUserAgent = originalRequest.newBuilder()
//      .header("User-Agent", userAgent)
//      .build()
//    return chain.proceed(requestWithUserAgent)
//  }

  //TODO remove. mocks the response
  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()

    if (originalRequest.url.toString().contains("/user_stats")) {

      val responseString =
        """{
          "promotions": [
          {
            "id": "GAMIFICATION",
            "current_amount": 2938120.58297987,
            "level": 8,
            "bonus": 18,
            "next_level_amount": 3500000,
            "bonus_earned": 452933.50307623664,
            "status": "ACTIVE",
            "bundle": true,
            "user_type": "UNDEFINED",
            "priority": 100,
            "gamification_type": "VIP",
            "approaching_next_level": null
          },
          {
            "id": "PACKAGE_DISTRIBUTION_PERK",
            "priority": 0,
            "start_date": 1658794592,
            "end_date": 1659609034,
            "current_progress": "0",
            "objective_progress": "0",
            "icon": "https://cdn6.aptoide.com/imgs/2/e/7/2e7440a8d6154c3a05e611a44c86296a_icon.png",
            "view_type": "DEFAULT",
            "linked_promotion_id": null,
            "details_link": "https://appcoins-trivial-drive-demo-sample.en.aptoide.com/",
            "app_name": "Appcoins Trivial Drive demo sample",
            "gamification_type": "VIP",
            "notification_title": "New Perk",
            "notification_description": "Receive an extra 5% Bonus in all your purchases, except AppCoins Credits, in Appcoins Trivial Drive demo sample.",
            "perk_description": "Receive an extra 5% Bonus in all your purchases."
          },
          {
            "id": "PACKAGE_DISTRIBUTION_PERK",
            "priority": 0,
            "start_date": 1658737567,
            "end_date": 1669263172,
            "current_progress": "0",
            "objective_progress": "0",
            "icon": "https://cdn6.aptoide.com/imgs/6/c/8/6c8dfb1fa8f2c7a6a8fefccf6976274c.png",
            "view_type": "DEFAULT",
            "linked_promotion_id": null,
            "details_link": "https://appcoins-trivial-drive-demo-sample.en.aptoide.com/",
            "app_name": "Appcoins Trivial Drive demo sample",
            "gamification_type": "STANDARD",
            "notification_title": "New Perk",
            "notification_description": "Receive an extra 5% Bonus in all your purchases, except AppCoins Credits, in Appcoins Trivial Drive demo sample.",
            "perk_description": "Receive an extra 5% Bonus in all your purchases."
          },
          {
            "id": "PROMO_CODE_PERK",
            "priority": 0,
            "start_date": 1658737567,
            "end_date": 1659609034,
            "current_progress": "0",
            "objective_progress": "0",
            "icon": "https://cdn6.aptoide.com/imgs/6/c/8/6c8dfb1fa8f2c7a6a8fefccf6976274c.png",
            "view_type": "DEFAULT",
            "linked_promotion_id": null,
            "details_link": "https://appcoins-trivial-drive-demo-sample.en.aptoide.com/",
            "app_name": "Appcoins Trivial Drive demo sample",
            "gamification_type": "STANDARD",
            "notification_title": "New Perk",
            "notification_description": "Receive an extra 5% Bonus in all your purchases, except AppCoins Credits, in Appcoins Trivial Drive demo sample.",
            "perk_description": "Receive an extra 5% Bonus in all your purchases."
          },
          {
            "id": "PROMO_CODE_PERK",
            "priority": 0,
            "start_date": 1658737567,
            "end_date": 1659609034,
            "current_progress": "0",
            "objective_progress": "0",
            "icon": "https://cdn6.aptoide.com/imgs/6/c/8/6c8dfb1fa8f2c7a6a8fefccf6976274c.png",
            "view_type": "DEFAULT",
            "linked_promotion_id": null,
            "details_link": "https://appcoins-trivial-drive-demo-sample.en.aptoide.com/",
            "app_name": "Appcoins Trivial Drive demo sample",
            "gamification_type": "VIP",
            "notification_title": "New Perk",
            "notification_description": "Receive an extra 5% Bonus in all your purchases, except AppCoins Credits, in Appcoins Trivial Drive demo sample.",
            "perk_description": "Receive an extra 5% Bonus in all your purchases."
          },
          {
            "id": "GAMIFICATION_LEVEL_UP",
            "priority": 32,
            "start_date": 1596672000,
            "end_date": 1669577600,
            "current_progress": "2938120.58",
            "objective_progress": "3500000.0",
            "icon": "https://apichain.catappult.io/appc/icons/perk_promotion.png",
            "view_type": "PROGRESS",
            "linked_promotion_id": "GAMIFICATION",
            "details_link": null,
            "app_name": null,
            "gamification_type": null,
            "notification_title": "Earn AppCoins Credits!",
            "notification_description": "Reach the next level and receive 6000 AppCoins Credits!",
            "perk_description": "Reach the next level and receive 6000 AppCoins Credits!"
          },
          {
            "id": "PROMO_CODE_PERK",
            "priority": 0,
            "start_date": 1659537567,
            "end_date": 1659609034,
            "current_progress": "0",
            "objective_progress": "0",
            "icon": "https://cdn6.aptoide.com/imgs/6/c/8/6c8dfb1fa8f2c7a6a8fefccf6976274c.png",
            "view_type": "DEFAULT",
            "linked_promotion_id": null,
            "details_link": "https://appcoins-trivial-drive-demo-sample.en.aptoide.com/",
            "app_name": "Appcoins Trivial Drive demo sample",
            "gamification_type": "VIP",
            "notification_title": "New Perk",
            "notification_description": "Receive an extra 5% Bonus in all your purchases, except AppCoins Credits, in Appcoins Trivial Drive demo sample.",
            "perk_description": "Receive an extra 5% Bonus in all your purchases."
          },
          {
            "id": "PROMO_CODE_PERK",
            "priority": 0,
            "start_date": 1659537567,
            "end_date": 1659609034,
            "current_progress": "0",
            "objective_progress": "0",
            "icon": "https://cdn6.aptoide.com/imgs/6/c/8/6c8dfb1fa8f2c7a6a8fefccf6976274c.png",
            "view_type": "DEFAULT",
            "linked_promotion_id": null,
            "details_link": "https://appcoins-trivial-drive-demo-sample.en.aptoide.com/",
            "app_name": "Appcoins Trivial Drive demo sample",
            "gamification_type": "STANDARD",
            "notification_title": "New Perk",
            "notification_description": "Receive an extra 5% Bonus in all your purchases, except AppCoins Credits, in Appcoins Trivial Drive demo sample.",
            "perk_description": "Receive an extra 5% Bonus in all your purchases."
          }
          ],
          "wallet_origin": "APTOIDE"
        }"""

      return chain.proceed(chain.request())
        .newBuilder()
        .header("User-Agent", userAgent)
        .code(200)
        .protocol(Protocol.HTTP_2)
        .message(responseString)
        .body(
          ResponseBody.create(
            "application/json".toMediaTypeOrNull(),
            responseString.toByteArray()))
        .addHeader("content-type", "application/json")
        .build()

    } else {
      val requestWithUserAgent = originalRequest.newBuilder()
        .header("User-Agent", userAgent)
        .build()
      return chain.proceed(requestWithUserAgent)
    }
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
