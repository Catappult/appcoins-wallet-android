package com.asfoundation.wallet.feature_flags.topup

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import it.czerwinski.android.hilt.annotations.BoundTo
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import javax.inject.Inject

@BoundTo(supertype = AndroidIdRepository::class)
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
