package com.asfoundation.wallet.identification

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.appcoins.wallet.core.analytics.analytics.partners.InstallerService
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import java.util.*
import javax.inject.Inject

class IdsRepository @Inject constructor(
  @ApplicationContext private val context: Context,
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource,
  private val userStatsLocalData: UserStatsLocalData,
  private val installerService: InstallerService
) {

  fun getAndroidId(): String {
    var androidId = commonsPreferencesDataSource.getAndroidId()
    if (androidId.isNotEmpty()) {
      return androidId
    }
    androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    commonsPreferencesDataSource.setAndroidId(androidId)
    return androidId
  }

  fun getActiveWalletAddress(): String {
    return commonsPreferencesDataSource.getCurrentWalletAddress() ?: ""
  }

  fun getGamificationLevel() = userStatsLocalData.getGamificationLevel()

  fun getInstallerPackage(packageName: String): Single<String> {
    return installerService.getInstallerPackageName(packageName)
  }

  fun getDeviceInfo(): DeviceInformation {
    return DeviceInformation(
      osVersion = Build.VERSION.RELEASE,
      brand = Build.BRAND,
      model = Build.MODEL,
      language = Locale.getDefault().language,
      isProbablyEmulator = isProbablyEmulator()
    )
  }

  private fun isProbablyEmulator(): Boolean {
    return (
        (Build.FINGERPRINT.startsWith("google/sdk_gphone_")
            && Build.FINGERPRINT.endsWith(":user/release-keys")
            && Build.MANUFACTURER == "Google"
            && Build.PRODUCT.startsWith("sdk_gphone_")
            && Build.BRAND == "google"
            && Build.MODEL.startsWith("sdk_gphone_")
            )
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || ("QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(
                  Build.MANUFACTURER,
                  ignoreCase = true
                )) //bluestacks
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HOST.startsWith("Build") //MSI App Player
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.PRODUCT == "google_sdk"
        )
  }
}
