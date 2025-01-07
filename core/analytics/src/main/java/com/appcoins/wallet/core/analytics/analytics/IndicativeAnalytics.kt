package com.appcoins.wallet.core.analytics.analytics

import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import it.czerwinski.android.hilt.annotations.BoundTo
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@BoundTo(supertype = AnalyticsSetup::class)
@Singleton
class IndicativeAnalytics @Inject constructor(
  @ApplicationContext private val context: Context,
) : AnalyticsSetup {

  var usrId: String = ""  // wallet address
  var superProperties: MutableMap<String, Any> = HashMap()

  companion object {
    const val ORIENTATION_PORTRAIT = "portrait"
    const val ORIENTATION_LANDSCAPE = "landscape"
    const val ORIENTATION_OTHER = "other"
    const val FIRST_PAYMENT = "first_payment"
    const val REGULAR_PAYMENT = "regular"
  }

  override fun setUserId(walletAddress: String) {
    usrId = walletAddress
  }

  override fun setGamificationLevel(level: Int) {
    superProperties.put(AnalyticsLabels.USER_LEVEL, level)
  }

  override fun setWalletOrigin(origin: String) {
    superProperties.put(AnalyticsLabels.WALLET_ORIGIN, origin)
  }

  override fun setPromoCode(code: String?, bonus: Double?, validity: Int?, appName: String?) {
    superProperties.put(AnalyticsLabels.PROMO_CODE, code ?: "")
  }

  fun setIndicativeSuperProperties(
    installerPackage: String,
    userLevel: Int,
    userId: String,
    hasGms: Boolean,
    walletOrigin: String,
    osVersion: String,
    brand: String,
    model: String,
    language: String,
    isEmulator: Boolean,
    ghOemId: String,
    promoCode: String,
    flavor: String,
    theme: String
  ) {

    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    superProperties.put(
      AnalyticsLabels.APTOIDE_PACKAGE,
      packageInfo.packageName
    )
    superProperties.put(AnalyticsLabels.VERSION_CODE, packageInfo.versionCode)
    superProperties.put(
      AnalyticsLabels.ENTRY_POINT,
      if (installerPackage.isEmpty()) "other" else installerPackage
    )
    superProperties.put(AnalyticsLabels.USER_LEVEL, userLevel)
    superProperties.put(AnalyticsLabels.HAS_GMS, hasGms)
    superProperties.put(AnalyticsLabels.WALLET_ORIGIN, walletOrigin)

    // device information:
    superProperties.put(AnalyticsLabels.OS_VERSION, osVersion)
    superProperties.put(AnalyticsLabels.BRAND, brand)
    superProperties.put(AnalyticsLabels.MODEL, model)
    superProperties.put(AnalyticsLabels.LANGUAGE, language)
    superProperties.put(AnalyticsLabels.IS_EMULATOR, isEmulator)
    superProperties.put(AnalyticsLabels.GAMES_HUB_OEMID, ghOemId)
    superProperties.put(AnalyticsLabels.PROMO_CODE, promoCode)  // should this be a user property?
    superProperties.put(AnalyticsLabels.FLAVOR, flavor)
    superProperties.put(AnalyticsLabels.THEME, theme)

    if (userId.isNotEmpty()) this.usrId = userId

  }

  fun findDeviceOrientation(): String {
    return when (context.resources.configuration.orientation) {
      Configuration.ORIENTATION_LANDSCAPE -> ORIENTATION_LANDSCAPE
      Configuration.ORIENTATION_PORTRAIT -> ORIENTATION_PORTRAIT
      Configuration.ORIENTATION_UNDEFINED -> ORIENTATION_OTHER
      else -> ORIENTATION_OTHER
    }
  }

  fun getIndicativeSuperProperties(): String {
    val labels = listOf(
      AnalyticsLabels.APTOIDE_PACKAGE,
      AnalyticsLabels.VERSION_CODE,
      AnalyticsLabels.ENTRY_POINT,
      AnalyticsLabels.USER_LEVEL,
      AnalyticsLabels.HAS_GMS,
      AnalyticsLabels.WALLET_ORIGIN,
      AnalyticsLabels.OS_VERSION,
      AnalyticsLabels.BRAND,
      AnalyticsLabels.MODEL,
      AnalyticsLabels.LANGUAGE,
      AnalyticsLabels.IS_EMULATOR,
      AnalyticsLabels.GAMES_HUB_OEMID,
      AnalyticsLabels.PROMO_CODE,
      AnalyticsLabels.FLAVOR,
      AnalyticsLabels.THEME
    )

    val jsonObject = JSONObject().apply {
      labels.forEach { label ->
        put(label, superProperties[label])
      }
    }

    return jsonObject.toString()
  }


}