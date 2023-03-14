package com.asfoundation.wallet.feature_flags.topup

import android.content.Context
import android.provider.Settings
import repository.PreferencesRepositoryType
import dagger.hilt.android.qualifiers.ApplicationContext
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = AndroidIdRepository::class)
class AndroidIdRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val sharedPreferencesRepository: PreferencesRepositoryType,
) : AndroidIdRepository {

  override fun getAndroidId(): String {
    var androidId = sharedPreferencesRepository.getAndroidId()
    if (androidId.isNotEmpty()) {
      return androidId
    }
    androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    sharedPreferencesRepository.setAndroidId(androidId)
    return androidId
  }
}
