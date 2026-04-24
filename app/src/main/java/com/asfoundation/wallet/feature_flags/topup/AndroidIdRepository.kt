package com.asfoundation.wallet.feature_flags.topup

import android.content.Context
import android.provider.Settings
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidIdRepositoryImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource,
) : AndroidIdRepository {

  override fun getAndroidId(): String {
    var androidId = commonsPreferencesDataSource.getAndroidId()
    if (androidId.isNotEmpty()) {
      return androidId
    }
    androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    commonsPreferencesDataSource.setAndroidId(androidId)
    return androidId
  }
}
