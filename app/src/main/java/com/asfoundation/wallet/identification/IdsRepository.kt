package com.asfoundation.wallet.identification

import android.content.ContentResolver
import android.provider.Settings
import android.text.TextUtils
import com.asfoundation.wallet.repository.PreferencesRepositoryType

class IdsRepository(private val contentResolver: ContentResolver,
                    private val sharedPreferencesRepository: PreferencesRepositoryType) {

  fun getAndroidId(): String {
    var androidId = sharedPreferencesRepository.getAndroidId()
    if (!TextUtils.isEmpty(androidId)) {
      return androidId
    }
    androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

    sharedPreferencesRepository.setAndroidId(androidId)
    return androidId
  }

}