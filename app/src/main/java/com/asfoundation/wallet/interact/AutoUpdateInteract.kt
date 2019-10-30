package com.asfoundation.wallet.interact

import android.content.pm.PackageManager
import io.reactivex.Single

class AutoUpdateInteract(private val autoUpdateService: AutoUpdateService,
                         private val localVersionCode: Int, private val currentMinSdk: Int,
                         private val packageManager: PackageManager,
                         private val walletPackageName: String) {

  fun getAutoUpdateModel(): Single<AutoUpdateModel> {
    return autoUpdateService.loadAutoUpdateModel()
  }

  fun hasSoftUpdate(updateVersionCode: Int, updatedMinSdk: Int): Boolean {
    return (localVersionCode < updateVersionCode && currentMinSdk >= updatedMinSdk)
  }

  fun isHardUpdateRequired(blackList: List<Int>, updateVersionCode: Int,
                           updateMinSdk: Int): Boolean {
    return blackList.contains(localVersionCode) && hasSoftUpdate(updateVersionCode, updateMinSdk)
  }

  fun retrieveRedirectUrl(): String {
    return when {
      isInstalled(APTOIDE_PACKAGE_NAME) -> APTOIDE_APPVIEW_URL
      isInstalled(PLAY_PACKAGE_NAME) -> PLAY_APPVIEW_URL + walletPackageName
      else -> APTOIDE_APPVIEW_URL
    }
  }

  private fun isInstalled(packageName: String): Boolean {
    return try {
      packageManager.getApplicationInfo(packageName, 0)
          .enabled
    } catch (exception: PackageManager.NameNotFoundException) {
      false
    }
  }

  companion object {
    private const val APTOIDE_PACKAGE_NAME = "cm.aptoide.pt"
    private const val PLAY_PACKAGE_NAME = "com.android.vending"
    private const val APTOIDE_APPVIEW_URL = "https://appcoins-wallet.en.aptoide.com/"
    private const val PLAY_APPVIEW_URL = "https://play.google.com/store/apps/details?id="
  }
}
