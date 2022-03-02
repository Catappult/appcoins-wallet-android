package com.asfoundation.wallet.identification

import android.content.Context
import android.provider.Settings
import com.appcoins.wallet.gamification.repository.UserStatsLocalData
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import javax.inject.Inject

class IdsRepository @Inject constructor(@ApplicationContext private val context: Context,
                                        private val sharedPreferencesRepository: PreferencesRepositoryType,
                                        private val userStatsLocalData: UserStatsLocalData,
                                        private val installerService: InstallerService) {

  fun getAndroidId(): String {
    var androidId = sharedPreferencesRepository.getAndroidId()
    if (androidId.isNotEmpty()) {
      return androidId
    }
    androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    sharedPreferencesRepository.setAndroidId(androidId)
    return androidId
  }

  fun getActiveWalletAddress(): String {
    return sharedPreferencesRepository.getCurrentWalletAddress() ?: ""
  }

  fun getGamificationLevel() = userStatsLocalData.getGamificationLevel()

  fun getInstallerPackage(packageName: String): Single<String> {
    return installerService.getInstallerPackageName(packageName)
  }
}