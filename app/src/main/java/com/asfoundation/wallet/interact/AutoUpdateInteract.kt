package com.asfoundation.wallet.interact

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.asfoundation.wallet.repository.AutoUpdateRepository
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Single

class AutoUpdateInteract(private val autoUpdateRepository: AutoUpdateRepository,
                         private val walletVersionCode: Int, private val deviceSdk: Int,
                         private val packageManager: PackageManager,
                         private val walletPackageName: String,
                         private val sharedPreferencesRepository: PreferencesRepositoryType) {

  fun getAutoUpdateModel(invalidateCache: Boolean = true): Single<AutoUpdateModel> {
    return autoUpdateRepository.loadAutoUpdateModel(invalidateCache)
  }

  fun hasSoftUpdate(updateVersionCode: Int, updatedMinSdk: Int): Boolean {
    return walletVersionCode < updateVersionCode && deviceSdk >= updatedMinSdk
  }

  fun isHardUpdateRequired(blackList: List<Int>, updateVersionCode: Int,
                           updateMinSdk: Int): Boolean {
    return blackList.contains(walletVersionCode) && hasSoftUpdate(updateVersionCode, updateMinSdk)
  }

  fun retrieveRedirectUrl(): String {
    return when {
      isInstalled(APTOIDE_PACKAGE_NAME) -> APTOIDE_APP_VIEW_URL
      isInstalled(PLAY_PACKAGE_NAME) -> PLAY_APP_VIEW_URL + walletPackageName
      else -> APTOIDE_APP_VIEW_URL
    }
  }

  fun buildUpdateIntent(): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(retrieveRedirectUrl()))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val appsList =
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    appsList?.let {
      for (info in appsList) {
        if (info.activityInfo.packageName == "cm.aptoide.pt") {
          intent.setPackage(info.activityInfo.packageName)
          break
        }
        if (info.activityInfo.packageName == "com.android.vending")
          intent.setPackage(info.activityInfo.packageName)
      }
    }
    return intent
  }

  private fun isInstalled(packageName: String): Boolean {
    return try {
      packageManager.getApplicationInfo(packageName, 0)
          .enabled
    } catch (exception: PackageManager.NameNotFoundException) {
      false
    }
  }

  fun shouldShowNotification(): Boolean {
    val savedTime = sharedPreferencesRepository.getUpdateNotificationSeenTime()
    val currentTime = System.currentTimeMillis()
    val timeToShowNextNotificationInMillis = 3600000 * 12
    return currentTime >= savedTime + timeToShowNextNotificationInMillis
  }

  fun saveSeenUpdateNotification() {
    sharedPreferencesRepository.setUpdateNotificationSeenTime(System.currentTimeMillis())
  }

  companion object {
    private const val APTOIDE_PACKAGE_NAME = "cm.aptoide.pt"
    private const val PLAY_PACKAGE_NAME = "com.android.vending"
    private const val APTOIDE_APP_VIEW_URL = "https://appcoins-wallet.en.aptoide.com/"
    private const val PLAY_APP_VIEW_URL = "https://play.google.com/store/apps/details?id="
  }
}
