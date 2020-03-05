package com.asfoundation.wallet.identification

import android.content.ContentResolver
import android.provider.Settings
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.Single

class IdsRepository(private val contentResolver: ContentResolver,
                    private val sharedPreferencesRepository: PreferencesRepositoryType,
                    private val installerService: InstallerService) {

  fun getAndroidId(): String {
    var androidId = sharedPreferencesRepository.getAndroidId()
    if (androidId.isNotEmpty()) {
      return androidId
    }
    androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

    sharedPreferencesRepository.setAndroidId(androidId)
    return androidId
  }

  fun getActiveWalletAddress(): String {
    return sharedPreferencesRepository.getCurrentWalletAddress() ?: ""
  }

  fun getGamificationLevel() = sharedPreferencesRepository.getGamificationLevel()

  fun getInstallerPackage(packageName: String) : Single<String> {
    return installerService.getInstallerPackageName(packageName)
  }

}