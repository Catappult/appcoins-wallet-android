package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class OemIdPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences,
) {
  fun putOemIdCache(key: String?, value: String?) =
    sharedPreferences.edit()
      .putString(key, value)
      .apply()

  fun getOemIdCache(key: String?) = sharedPreferences.getString(key, "")

  fun setCurrentOemId(value: String?) =
    sharedPreferences.edit()
      .putString(CURRENT_OEMID, value)
      .apply()

  fun getCurrentOemId() = sharedPreferences.getString(CURRENT_OEMID, "") ?: ""

  fun setIsGameFromGameshub(value: Boolean) =
    sharedPreferences.edit()
      .putBoolean(IS_GAME_FROM_GAMESHUB_KEY, value)
      .apply()

  fun getIsGameFromGameshub() = sharedPreferences.getBoolean(IS_GAME_FROM_GAMESHUB_KEY, false)

  fun setOemIdForPackage(packageName: String, oemid: String) =
    sharedPreferences.edit()
      .putString("${CLIENT_SIDE_CACHED_OEMID}${packageName}", oemid)
      .apply()

  fun getOemIdForPackage(packageName: String) =
    sharedPreferences.getString("${CLIENT_SIDE_CACHED_OEMID}${packageName}", "") ?: ""

  companion object {
    private const val CURRENT_OEMID = "current_oemid"
    private const val IS_GAME_FROM_GAMESHUB_KEY = "game_from_gameshub"
    private const val CLIENT_SIDE_CACHED_OEMID = "client_side_cached_oemid"
  }
}
