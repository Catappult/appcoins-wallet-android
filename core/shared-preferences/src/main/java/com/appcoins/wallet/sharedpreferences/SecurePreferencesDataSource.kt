package com.appcoins.wallet.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Used to store and retrieve sensitive data as SharedPreferences
 */
@Singleton
class SecurePreferencesDataSource @Inject constructor(
  @ApplicationContext private val context: Context
) {

  companion object {
    const val FILE_NAME = "sec_shr_prf"
  }

  val sharedPreferences: SharedPreferences by lazy {
    val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
      .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
      .build()
    EncryptedSharedPreferences.create(
      context, FILE_NAME, masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences
        .PrefValueEncryptionScheme.AES256_GCM)
  }

  fun saveString(key: String, value: String) =
    sharedPreferences.edit()
      .putString(key, value)
      .apply()

  fun remove(vararg keys: String) =
    sharedPreferences.edit()
      .apply {
        keys.forEach { key -> remove(key) }
      }
      .apply()

  /**
   * Saves list of strings denoted as pairs of <Key, Value>
   */
  fun saveStrings(vararg values: Pair<String, String>) =
    sharedPreferences.edit()
      .apply {
        values.forEach { pair -> putString(pair.first, pair.second) }
      }
      .apply()

  fun getString(key: String, defValue: String?) = sharedPreferences.getString(key, defValue)

  fun getBoolean(key: String, defValue: Boolean) = sharedPreferences.getBoolean(key, defValue)
}