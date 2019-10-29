package com.asfoundation.wallet.interact

import android.content.pm.PackageManager
import io.reactivex.Single

class AutoUpdateInteract(private val autoUpdateRepository: AutoUpdateRepository,
                         private val localVersionCode: Int, private val currentMinSdk: Int,
                         private val packageManager: PackageManager,
                         private val walletPackageName: String) {

  fun getAutoUpdateModel(): Single<AutoUpdateModel> {
    return autoUpdateRepository.loadAutoUpdateModel()
  }

  fun hasSoftUpdate(updateVersionCode: Int, updatedMinSdk: Int): Boolean {
    return (localVersionCode < updateVersionCode && currentMinSdk >= updatedMinSdk)
  }

  fun isHardUpdateRequired(blackList: List<Int>, updateVersionCode: Int,
                           updateMinSdk: Int): Boolean {
    return blackList.contains(localVersionCode) && hasSoftUpdate(updateVersionCode, updateMinSdk)
  }

  fun retrieveRedirectUrl(): Single<String> {
    return when {
      isInstalled("cm.aptoide.pt") -> Single.just("https://appcoins-wallet.en.aptoide.com/")
      isInstalled("com.android.vending") -> Single.just(
          "https://play.google.com/store/apps/details?id=$walletPackageName&hl=it")
      else -> Single.just("https://appcoins-wallet.en.aptoide.com/")
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
}
